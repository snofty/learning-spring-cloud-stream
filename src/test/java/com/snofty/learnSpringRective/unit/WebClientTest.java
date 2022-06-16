package com.snofty.learnSpringRective.unit;

import org.springframework.web.reactive.function.client.WebClient;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.URI;

public class WebClientTest {
    @Test
    public void testSimple() {
        WebClient webClient = WebClient.create();
        Mono<String> ResponseMono = webClient.get().uri(URI.create("http://localhost:8080/getRandom"))
                .retrieve()
                .bodyToMono(String.class);

    }
}
