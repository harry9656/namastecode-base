package com.namastecode.rag_spring_ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor.FILTER_EXPRESSION;

@Service
public class ChatService implements Serializable {

    private static final String SYSTEM_PROMPT = """
            You are an expert in various domains, capable of providing detailed and accurate information.
            Using the context provided by recent conversations, answer the new question in a concise and informative manner.
            Limit your answer to a maximum of three sentences.
            Your response is always a simple text.
            """;
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public ChatService(ChatClient.Builder chatClientBuilder,
                       VectorStore vectorStore) {
        chatMemory = new InMemoryChatMemory();
        QuestionAnswerAdvisor questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore);
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        questionAnswerAdvisor,
                        new SimpleLoggerAdvisor())
                .build();
    }

    public ChatResponse ask(String question, String conversationId, Set<String> sourceFiles) {
        String sourcesFilter = String.format("source in ['%s']", String.join("','", sourceFiles));
        return chatClient.prompt()
                .user(question)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .advisors(spec -> spec.param(FILTER_EXPRESSION, sourcesFilter))
                .call()
                .chatResponse();
    }

    public List<Message> getChatHistory(String conversationId) {
        return chatMemory.get(conversationId, 100);
    }

    public void clearChatMemory(String conversationId) {
        chatMemory.clear(conversationId);
    }
}
