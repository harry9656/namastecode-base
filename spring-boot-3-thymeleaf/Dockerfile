FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

COPY target/spring-boot-3-thymeleaf-0.0.1-SNAPSHOT-jar-with-dependencies.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]