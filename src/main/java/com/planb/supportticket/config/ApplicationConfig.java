package com.planb.supportticket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationConfig {

    private final Environment environment;

    @Bean
    @Profile("dev")
    public void devEnvironmentSetup() {
        log.info("Application is running in DEVELOPMENT mode");
        log.info("Database URL: {}", environment.getProperty("spring.datasource.url"));
    }

    @Bean
    @Profile("prod")
    public void prodEnvironmentSetup() {
        log.info("Application is running in PRODUCTION mode");
        log.info("Active profiles: {}", String.join(", ", environment.getActiveProfiles()));
    }
}
