package org.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfiguration {

    @Bean
    public CustomInMemoryRateLimiter customInMemoryRateLimiter() {
        return new CustomInMemoryRateLimiter();
    }

    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getHeaders().getFirst("Api-Key"));
    }
}
