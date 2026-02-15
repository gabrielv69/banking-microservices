package com.banking.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
/**
 * Configuration for WebClient to communicate with other microservices
 */
@Configuration
public class WebClientConfig {

    /**
     * Base URL for customer-service
     */
    @Value("${customer-service.base-url}")
    private String customerServiceBaseUrl;

    /**
     * Creates a WebClient bean configured for customer-service
     * @return WebClient configured for customer-service
     */
    @Bean
    public WebClient customerServiceWebClient() {
        return WebClient.builder()
                .baseUrl(customerServiceBaseUrl)
                .build();
    }

}