package com.namastecode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class SpringBoot3Application {

    public static void main(String[] args) {
        SpringApplication.run(SpringBoot3Application.class, args);
    }

    @EventListener
    public void onServletWebServerInitialized(ServletWebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        System.out.println("Application is running on port: " + port);
    }

}
