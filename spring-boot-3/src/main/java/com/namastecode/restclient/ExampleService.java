package com.namastecode.restclient;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.RestClient;

@EnableRetry
public class ExampleService {

    private final RestClient restClient;

    public ExampleService(RestClient restClient) {
        this.restClient = restClient;
    }

    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 1000))
    public String performRestCall() {
        return restClient.get()
            .uri("https://example.com")
            .retrieve()
            .body(String.class);
    }

    @Retryable(maxAttempts = 5, retryFor = { CustomException.class })
    public String performRestCallWithCustomException() {
        return restClient.get()
            .uri("https://example.com")
            .retrieve()
            .body(String.class);
    }
}
