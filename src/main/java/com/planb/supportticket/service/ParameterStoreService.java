package com.planb.supportticket.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParameterStoreService {

    private final SsmClient ssmClient;
    
    @Value("${aws.ssm.parameter-path}")
    private String parameterPath;
    
    /**
     * Gets a parameter from AWS Parameter Store
     */
    public Optional<String> getParameter(String parameterName) {
        try {
            String fullParameterName = parameterPath + parameterName;
            
            GetParameterRequest request = GetParameterRequest.builder()
                    .name(fullParameterName)
                    .withDecryption(true)
                    .build();
            
            GetParameterResponse response = ssmClient.getParameter(request);
            return Optional.of(response.parameter().value());
        } catch (ParameterNotFoundException e) {
            log.warn("Parameter not found: {}", parameterName);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error getting parameter: {}", parameterName, e);
            throw new RuntimeException("Failed to get parameter from Parameter Store", e);
        }
    }
    
    /**
     * Gets all parameters under a specific path
     */
    public Map<String, String> getParametersByPath(String path) {
        try {
            String fullPath = parameterPath + path;
            
            GetParametersByPathRequest request = GetParametersByPathRequest.builder()
                    .path(fullPath)
                    .recursive(true)
                    .withDecryption(true)
                    .build();
            
            Map<String, String> parameters = new HashMap<>();
            GetParametersByPathResponse response;
            
            do {
                response = ssmClient.getParametersByPath(request);
                List<Parameter> parameterList = response.parameters();
                
                for (Parameter parameter : parameterList) {
                    // Extract the parameter name without the full path
                    String name = parameter.name().replace(fullPath, "");
                    if (name.startsWith("/")) {
                        name = name.substring(1);
                    }
                    parameters.put(name, parameter.value());
                }
                
                // If there are more parameters, update the request with the next token
                request = request.toBuilder()
                        .nextToken(response.nextToken())
                        .build();
                
            } while (response.nextToken() != null);
            
            return parameters;
        } catch (Exception e) {
            log.error("Error getting parameters by path: {}", path, e);
            throw new RuntimeException("Failed to get parameters from Parameter Store", e);
        }
    }
    
    /**
     * Puts a parameter in AWS Parameter Store
     */
    public void putParameter(String parameterName, String value, boolean secure) {
        try {
            String fullParameterName = parameterPath + parameterName;
            
            PutParameterRequest.Builder requestBuilder = PutParameterRequest.builder()
                    .name(fullParameterName)
                    .value(value)
                    .overwrite(true);
            
            if (secure) {
                requestBuilder.type("SecureString");
            } else {
                requestBuilder.type("String");
            }
            
            ssmClient.putParameter(requestBuilder.build());
            log.info("Parameter saved successfully: {}", parameterName);
        } catch (Exception e) {
            log.error("Error putting parameter: {}", parameterName, e);
            throw new RuntimeException("Failed to put parameter in Parameter Store", e);
        }
    }
}
