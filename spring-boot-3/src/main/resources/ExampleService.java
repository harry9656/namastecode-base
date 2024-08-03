package com.namastecode.restclient;
@EnableRetry
public class ExampleService {
    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 1000))
    public String performRestCall() {
        return restClient
        .get()
        .uri("https://example.com")
        .retrieve()
        .body(String.class);
    }
