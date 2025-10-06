package com.emiray.goldshop.price;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class GoldPriceHealth implements HealthIndicator {

    private final GoldPriceService svc;

    public GoldPriceHealth(GoldPriceService svc) {
        this.svc = svc;
    }

    @Override
    public Health health() {
        Instant last = svc.getLastSuccessAt();
        Double price = svc.getLastSuccessfulUsdPerGram();
        String error = svc.getLastErrorMessage();

        // Consider service healthy if last successful fetch was within the last 30 minutes
        boolean isFresh = svc.isFresh(Duration.ofMinutes(30));

        Health.Builder status = isFresh ? Health.up() : Health.status("DEGRADED");

        return status
                .withDetail("lastSuccessAt", last)
                .withDetail("lastUsdPerGram", price)
                .withDetail("lastError", error)
                .build();
    }
}
