package org.example;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringCloudGatewayApplicationTest {

    private WireMockServer wireMockServer;

    @BeforeEach
    public void setup() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());

        // Define a mock response
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/test"))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withBody("Hello from the mock server!")));
    }

    @AfterEach
    public void teardown() {
        wireMockServer.stop();
    }


    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testAccessWithTokenAndMockServer() {
        // Update the route to use the WireMock server
        int wireMockPort = wireMockServer.port();
        String mockServerUri = "http://localhost:" + wireMockPort;

        webTestClient.get()
            .uri("/api/test")
            .header("Authorization", "Bearer valid_token")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class).isEqualTo("Hello from the mock server!");
    }

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
