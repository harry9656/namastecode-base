package com.namastecode.restclient;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
class OpenAIRestClientTest {

    @Test
    void askQuestion() {
        OpenAIRestClient client = new OpenAIRestClient();
        client.askQuestion();
    }
}