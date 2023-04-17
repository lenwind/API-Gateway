package org.example.config;

import org.example.config.CustomInMemoryRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomInMemoryRateLimiterTest {

    private CustomInMemoryRateLimiter customInMemoryRateLimiter;

    @BeforeEach
    void setUp() {
        customInMemoryRateLimiter = new CustomInMemoryRateLimiter();
        CustomInMemoryRateLimiter.Config config = new CustomInMemoryRateLimiter.Config()
            .setReplenishRate(10)
            .setBurstCapacity(20);
        customInMemoryRateLimiter.getConfig().set(config);
    }

    @Test
    void shouldAllowRequestWhenWithinRateLimit() {
        String routeId = "my_route";
        String id = "test_id";

        Mono<RateLimiter.Response> response = customInMemoryRateLimiter.isAllowed(routeId, id);

        ResponseEntity<Void> expectedResponse = ResponseEntity.status(HttpStatus.OK).build();
        assertEquals(expectedResponse, response.block());
    }

    @Test
    void shouldDenyRequestWhenExceedingRateLimit() {
        String routeId = "my_route";
        String id = "test_id";

        for (int i = 0; i < 20; i++) {
            customInMemoryRateLimiter.isAllowed(routeId, id).block();
        }

        Mono<RateLimiter.Response> response = customInMemoryRateLimiter.isAllowed(routeId, id);

        ResponseEntity<Void> expectedResponse = ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        assertEquals(expectedResponse, response.block());
    }
}
