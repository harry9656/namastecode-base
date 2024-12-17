package com.namastecode.restclient;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Logger;

public class RestClientExample {

    private Logger log = Logger.getLogger(this.getClass().getName());

    public RestClientExample() {
        RestClient simpleRestClient = RestClient.create();
        RestTemplate restTemplate = new RestTemplate();
        RestClient restClientFromRestTemplate = RestClient.create(restTemplate);

        RestClient customRestClient = RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory())
                .messageConverters(converters -> converters.add(new MappingJackson2HttpMessageConverter()))
                .baseUrl("https://example.com")
                .defaultHeader("Authorization", "Bearer your-token")
                .defaultHeader("Content-Type", "application/json")
                .build();


        // GET request
        String stringResponse = simpleRestClient.get()
                .uri("https://awesomeapi.com/string/")
                .retrieve()
                .body(String.class);

        record Item(String id, String name, double price) {
        } // JSON: {"id": 123, "name": "Product", "price": 99.99}

        Item item = simpleRestClient.get()
                .uri("https://awesomeapi.com/item/{id}", 123)
                .retrieve()
                .body(Item.class);

        // POST request
        record ItemRequest(String name, double price) {
        } // JSON: {"name": "Product", "price": 99.99}
        ItemRequest newItem = new ItemRequest("Product", 99.99);

        Item createdItem = simpleRestClient.post()
                .uri("https://awesomeapi.com/item/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(newItem)
                .retrieve()
                .body(Item.class);

        // PUT request
        ItemRequest updatedItem = new ItemRequest("Updated Item", 129.99);

        Item updated = simpleRestClient.put()
                .uri("https://awesomeapi.com/item/{id}", 123)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updatedItem)
                .retrieve()
                .body(Item.class);

        // PUT request with query parameters
        Item updatedWithQuery = simpleRestClient.put()
                .uri(uriBuilder ->
                        uriBuilder.path("https://awesomeapi.com/item/")
                                .queryParam("id", 123)
                                .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(updatedItem)
                .retrieve()
                .body(Item.class);

        // DELETE request
        simpleRestClient.delete()
                .uri("https://awesomeapi.com/item/{id}", 123)
                .retrieve()
                .toBodilessEntity();

        // onStatus handler
        String id = "123";
        item = simpleRestClient.get()
                .uri("https://awesomeapi.com/item/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new RuntimeException("Item not found with id: " + id);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new RuntimeException("Service is currently unavailable");
                })
                .body(Item.class);

        // exchange() example
        Item itemResponse = simpleRestClient.get()
                .uri("/products/{id}", id)
                .exchange((request, response) -> {

                    HttpHeaders headers = response.getHeaders();
                    String etag = headers.getETag();

                    if (response.getStatusCode().is4xxClientError()) {
                        throw new RuntimeException("Item not found with id: " + id);
                    } else if (response.getStatusCode().is5xxServerError()) {
                        throw new RuntimeException("Service is currently unavailable");
                    }
                    log.info("Got request with ETAG: " + etag);
                    return response.bodyTo(Item.class);
                });
    }
}
