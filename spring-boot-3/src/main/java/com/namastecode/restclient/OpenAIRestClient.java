package com.namastecode.restclient;

import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.logging.Logger;

public class OpenAIRestClient {

    private final RestClient openAiClient;

    record ChatRequest(String model, List<Message> messages) {}
    record Message(String role, String content) {}
    record ChatResponse(List<Choice> choices) {}
    record Choice(Message message) {}

    private static Logger log = Logger.getLogger(OpenAIRestClient.class.getName());

    public OpenAIRestClient() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        openAiClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public void askQuestion(){
        ChatRequest request = new ChatRequest(
                "gpt-4o-mini",
                List.of(new Message("user", "What is the brightest star in our galaxy?"))
        );

        ChatResponse response = openAiClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(ChatResponse.class);

        log.info("ChatGPT: " + response.choices().get(0).message().content());
    }
}
