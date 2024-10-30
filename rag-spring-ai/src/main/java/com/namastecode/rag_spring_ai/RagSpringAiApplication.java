package com.namastecode.rag_spring_ai;

import com.vaadin.flow.component.page.AppShellConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RagSpringAiApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(RagSpringAiApplication.class, args);
    }

}
