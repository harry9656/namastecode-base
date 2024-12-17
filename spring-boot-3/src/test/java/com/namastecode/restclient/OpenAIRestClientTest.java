package com.namastecode.restclient;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIRestClientTest {

    @Test
    void askQuestion() {
        OpenAIRestClient client = new OpenAIRestClient();
        client.askQuestion();
    }
}