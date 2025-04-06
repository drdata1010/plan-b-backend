package com.planb.supportticket.config.aws;

import com.planb.supportticket.exception.AWSServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration service for retrieving and caching parameters from AWS Parameter Store.
 * Provides methods for retrieving secrets and configuration values.
 */
@Service
@Configuration
@RequiredArgsConstructor
@Slf4j
public class AWSParameterStoreConfig {

    private final SsmClient ssmClient;
    
    @Value("${aws.ssm.parameter-path}")
    private String parameterPath;
    
    @Value("${aws.ssm.cache-ttl-seconds:300}")
    private long cacheTtlSeconds;
    
    @Value("${aws.ssm.preload-parameters:false}")
    private boolean preloadParameters;
    
    // Cache for parameters to reduce API calls
    private final Map<String, CachedParameter> parameterCache = new ConcurrentHashMap<>();
    
    /**
     * Initializes the parameter store configuration.
     * Preloads parameters if configured to do so.
     */
    @PostConstruct
    public void init() {
        if (preloadParameters) {
            log.info("Preloading parameters from AWS Parameter Store");
            try {
                preloadParametersFromPath(parameterPath);
                log.info("Successfully preloaded {} parameters from AWS Parameter Store", parameterCache.size());
            } catch (Exception e) {
                log.error("Failed to preload parameters from AWS Parameter Store", e);
                // Don't throw exception here to allow application to start even if parameter store is not available
            }
        }
    }
    
    /**
     * Gets a parameter from AWS Parameter Store.
     * Uses cache if available and not expired.
     *
     * @param parameterName The name of the parameter (without the path prefix)
     * @return An Optional containing the parameter value, or empty if not found
     * @throws AWSServiceException if retrieving the parameter fails
     */
    public Optional<String> getParameter(String parameterName) {
        return getParameter(parameterName, false);
    }
    
    /**
     * Gets a parameter from AWS Parameter Store.
     * Uses cache if available and not expired.
     *
     * @param parameterName The name of the parameter (without the path prefix)
     * @param forceRefresh Whether to force a refresh from the parameter store
     * @return An Optional containing the parameter value, or empty if not found
     * @throws AWSServiceException if retrieving the parameter fails
     */
    public Optional<String> getParameter(String parameterName, boolean forceRefresh) {
        String fullParameterName = buildFullParameterName(parameterName);
        
        // Check cache first if not forcing refresh
        if (!forceRefresh) {
            CachedParameter cachedParameter = parameterCache.get(fullParameterName);
            if (cachedParameter != null && !cachedParameter.isExpired()) {
                log.debug("Using cached parameter: {}", parameterName);
                return Optional.of(cachedParameter.getValue());
            }
        }
        
        // Retrieve from Parameter Store
        try {
            GetParameterRequest request = GetParameterRequest.builder()
                    .name(fullParameterName)
                    .withDecryption(true)
                    .build();
            
            GetParameterResponse response = ssmClient.getParameter(request);
            String value = response.parameter().value();
            
            // Update cache
            updateCache(fullParameterName, value);
            
            log.debug("Successfully retrieved parameter: {}", parameterName);
            return Optional.of(value);
        } catch (ParameterNotFoundException e) {
            log.warn("Parameter not found: {}", parameterName);
            return Optional.empty();
        } catch (SsmException e) {
            log.error("SSM error retrieving parameter {}: {}", parameterName, e.getMessage());
            throw new AWSServiceException("SSM", "getParameter", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error retrieving parameter {}: {}", parameterName, e.getMessage());
            throw new AWSServiceException("SSM", "getParameter", "Unexpected error retrieving parameter", e);
        }
    }
    
    /**
     * Gets a secret parameter from AWS Parameter Store.
     * This is a convenience method for retrieving secure strings.
     *
     * @param secretName The name of the secret parameter (without the path prefix)
     * @return An Optional containing the secret value, or empty if not found
     * @throws AWSServiceException if retrieving the secret fails
     */
    public Optional<String> getSecret(String secretName) {
        return getParameter(secretName);
    }
    
    /**
     * Gets all parameters under a specific path.
     *
     * @param path The path to get parameters from (relative to the base parameter path)
     * @param recursive Whether to recursively get parameters from subpaths
     * @param forceRefresh Whether to force a refresh from the parameter store
     * @return A map of parameter names to values
     * @throws AWSServiceException if retrieving the parameters fails
     */
    public Map<String, String> getParametersByPath(String path, boolean recursive, boolean forceRefresh) {
        String fullPath = buildFullParameterName(path);
        if (!fullPath.endsWith("/")) {
            fullPath += "/";
        }
        
        // If not forcing refresh, check if we can use cached values
        if (!forceRefresh) {
            Map<String, String> cachedParams = getCachedParametersByPath(fullPath);
            if (!cachedParams.isEmpty()) {
                log.debug("Using cached parameters for path: {}", path);
                return cachedParams;
            }
        }
        
        try {
            GetParametersByPathRequest request = GetParametersByPathRequest.builder()
                    .path(fullPath)
                    .recursive(recursive)
                    .withDecryption(true)
                    .build();
            
            Map<String, String> parameters = new HashMap<>();
            GetParametersByPathResponse response;
            
            do {
                response = ssmClient.getParametersByPath(request);
                List<Parameter> parameterList = response.parameters();
                
                for (Parameter parameter : parameterList) {
                    // Extract the parameter name without the full path
                    String name = parameter.name();
                    String value = parameter.value();
                    
                    // Update cache
                    updateCache(name, value);
                    
                    // Extract relative name for the result map
                    String relativeName = name.replace(fullPath, "");
                    parameters.put(relativeName, value);
                }
                
                // If there are more parameters, update the request with the next token
                if (response.nextToken() != null) {
                    request = request.toBuilder()
                            .nextToken(response.nextToken())
                            .build();
                }
                
            } while (response.nextToken() != null);
            
            log.info("Retrieved {} parameters from path: {}", parameters.size(), path);
            return parameters;
        } catch (SsmException e) {
            log.error("SSM error retrieving parameters by path {}: {}", path, e.getMessage());
            throw new AWSServiceException("SSM", "getParametersByPath", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error retrieving parameters by path {}: {}", path, e.getMessage());
            throw new AWSServiceException("SSM", "getParametersByPath", 
                    "Unexpected error retrieving parameters by path", e);
        }
    }
    
    /**
     * Gets all parameters under a specific path.
     * Uses default values for recursive (true) and forceRefresh (false).
     *
     * @param path The path to get parameters from (relative to the base parameter path)
     * @return A map of parameter names to values
     * @throws AWSServiceException if retrieving the parameters fails
     */
    public Map<String, String> getParametersByPath(String path) {
        return getParametersByPath(path, true, false);
    }
    
    /**
     * Clears the parameter cache.
     */
    public void clearCache() {
        log.info("Clearing parameter cache");
        parameterCache.clear();
    }
    
    /**
     * Preloads parameters from a path into the cache.
     *
     * @param path The path to preload parameters from
     */
    private void preloadParametersFromPath(String path) {
        try {
            GetParametersByPathRequest request = GetParametersByPathRequest.builder()
                    .path(path)
                    .recursive(true)
                    .withDecryption(true)
                    .build();
            
            GetParametersByPathResponse response;
            
            do {
                response = ssmClient.getParametersByPath(request);
                List<Parameter> parameterList = response.parameters();
                
                for (Parameter parameter : parameterList) {
                    updateCache(parameter.name(), parameter.value());
                }
                
                // If there are more parameters, update the request with the next token
                if (response.nextToken() != null) {
                    request = request.toBuilder()
                            .nextToken(response.nextToken())
                            .build();
                }
                
            } while (response.nextToken() != null);
            
        } catch (SsmException e) {
            log.error("SSM error preloading parameters: {}", e.getMessage());
            throw new AWSServiceException("SSM", "preloadParametersFromPath", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error preloading parameters: {}", e.getMessage());
            throw new AWSServiceException("SSM", "preloadParametersFromPath", 
                    "Unexpected error preloading parameters", e);
        }
    }
    
    /**
     * Updates the parameter cache with a new value.
     *
     * @param name The full parameter name
     * @param value The parameter value
     */
    private void updateCache(String name, String value) {
        parameterCache.put(name, new CachedParameter(value, System.currentTimeMillis() + (cacheTtlSeconds * 1000)));
    }
    
    /**
     * Gets cached parameters by path.
     *
     * @param path The path to get parameters from
     * @return A map of parameter names to values
     */
    private Map<String, String> getCachedParametersByPath(String path) {
        Map<String, String> result = new HashMap<>();
        
        for (Map.Entry<String, CachedParameter> entry : parameterCache.entrySet()) {
            if (entry.getKey().startsWith(path) && !entry.getValue().isExpired()) {
                String relativeName = entry.getKey().replace(path, "");
                result.put(relativeName, entry.getValue().getValue());
            }
        }
        
        return result;
    }
    
    /**
     * Builds the full parameter name by prepending the parameter path.
     *
     * @param parameterName The parameter name
     * @return The full parameter name
     */
    private String buildFullParameterName(String parameterName) {
        if (parameterName.startsWith("/")) {
            parameterName = parameterName.substring(1);
        }
        
        String fullPath = parameterPath;
        if (!fullPath.endsWith("/")) {
            fullPath += "/";
        }
        
        return fullPath + parameterName;
    }
    
    /**
     * Inner class for caching parameters with expiration.
     */
    private static class CachedParameter {
        private final String value;
        private final long expirationTime;
        
        public CachedParameter(String value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }
        
        public String getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }
}
