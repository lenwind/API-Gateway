package org.example.config;

import org.example.config.CustomInMemoryRateLimiter;

import java.time.Duration;
import java.time.Instant;

public class TokenBucket {

    private final CustomInMemoryRateLimiter.Config config;
    private int tokens;
    private Instant lastRefill;

    public TokenBucket(CustomInMemoryRateLimiter.Config config) {
        this.config = config;
        this.tokens = config.getBurstCapacity();
        this.lastRefill = Instant.now();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (tokens > 0) {
            tokens--;
            return true;
        }
        return false;
    }

    private void refill() {
        Instant now = Instant.now();
        Duration durationSinceLastRefill = Duration.between(lastRefill, now);
        long newTokens = durationSinceLastRefill.toMillis() * config.getReplenishRate() / 1000;
        if (newTokens > 0) {
            lastRefill = now;
        }
        tokens = Math.min(config.getBurstCapacity(), (int) (tokens + newTokens));
    }
}
