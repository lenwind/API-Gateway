package org.example.config;

import org.springframework.cloud.gateway.filter.ratelimit.AbstractRateLimiter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomInMemoryRateLimiter extends AbstractRateLimiter<CustomInMemoryRateLimiter.Config> {

    public static final String CONFIGURATION_PROPERTY_NAME = "custom-rate-limiter";
    private final ConcurrentHashMap<String, AtomicInteger> rateLimiterMap = new ConcurrentHashMap<>();

    public CustomInMemoryRateLimiter() {
        super(Config.class, CONFIGURATION_PROPERTY_NAME, null);
    }

    @Override
    public Mono<Response> isAllowed(String routeId, String id) {
        Config config = getConfig().get(routeId);
        if (config != null) {
            AtomicInteger atomicInteger = getAtomicInteger(routeId, id);
            if (atomicInteger.get() > config.getBurstCapacity()) {
                HttpHeaders headers = createHeaders(atomicInteger, config);
                return Mono.just(new Response(false, headers.toSingleValueMap()));
            }

            atomicInteger.incrementAndGet();
            Duration delay = Duration.ofSeconds(1L);
            Mono.delay(delay)
                .subscribe(v -> atomicInteger.decrementAndGet());

            return Mono.just(new Response(true, null));
        } else {
            return Mono.just(new Response(false, null));
        }
    }

    private AtomicInteger getAtomicInteger(String routeId, String id) {
        String key = routeId + ":" + id;
        rateLimiterMap.putIfAbsent(key, new AtomicInteger(0));
        return rateLimiterMap.get(key);
    }

    private HttpHeaders createHeaders(AtomicInteger atomicInteger, Config config) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RateLimit-Replenish-Rate", String.valueOf(config.getReplenishRate()));
        headers.add("X-RateLimit-Burst-Capacity", String.valueOf(config.getBurstCapacity()));
        headers.add("X-RateLimit-Remaining", String.valueOf(Math.max(0, config.getBurstCapacity() - atomicInteger.get())));
        return headers;
    }

    public static class Config {
        private int replenishRate;
        private int burstCapacity;

        public int getReplenishRate() {
            return replenishRate;
        }

        public Config setReplenishRate(int replenishRate) {
            this.replenishRate = replenishRate;
            return this;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public Config setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
            return this;
        }
    }
}
