package com.tistory.kmmoon.frankit.infrastructure.config;

import com.tistory.kmmoon.frankit.infrastructure.logging.ApplicationLogger;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {
    @Bean
    public ApplicationLogger applicationLogger() {
        return mock(ApplicationLogger.class);
    }
}
