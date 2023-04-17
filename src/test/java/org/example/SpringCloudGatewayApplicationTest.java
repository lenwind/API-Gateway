package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringCloudGatewayApplicationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testAccessWithoutToken() {
        webTestClient.get()
            .uri("/api/test")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    public void testAccessWithToken() {
        webTestClient.get()
            .uri("/api/test")
            .header("Authorization", "Bearer valid_token")
            .exchange()
            .expectStatus().isOk();
    }
}
