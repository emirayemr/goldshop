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
        Double  price = svc.getLastSuccessfulUsdPerGram();
        String  err = svc.getLastErrorMessage();

        // Son 30 dakika içinde başarılı fetch olduysa UP; değilse DEGRADE
        boolean fresh = svc.isFresh(Duration.ofMinutes(30));

        Health.Builder h = fresh ? Health.up() : Health.status("DEGRADED");
        return h
                .withDetail("lastSuccessAt", last)
                .withDetail("lastUsdPerGram", price)
                .withDetail("lastError", err)
                .build();
    }
}
