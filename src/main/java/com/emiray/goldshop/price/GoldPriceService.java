package com.emiray.goldshop.price;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
public class GoldPriceService {

    // ---- config ----
    private final String provider;
    private final String apiKey;
    private final double fallback;
    private final RestClient http;

    // ---- health telemetry ----
    private volatile Instant lastSuccessAt;
    private volatile Double lastSuccessfulUsdPerGram;
    private volatile String lastErrorMessage;

    public GoldPriceService(
            RestClient.Builder builder,
            @Value("${app.gold-price.provider:dummy}") String provider,
            @Value("${app.gold-price.api-key:}") String apiKey,
            @Value("${app.gold-price.fallback:75.0}") double fallback,
            @Value("${app.http.timeout-ms:3000}") long timeoutMs
    ) {
        this.provider = provider;
        this.apiKey = apiKey;
        this.fallback = fallback;

        // RestClient.Builder'da connectTimeout/readTimeout yok.
        // Timeout'ları request factory üzerinden veriyoruz.
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        int t = (int) Math.min(timeoutMs, Integer.MAX_VALUE);
        f.setConnectTimeout(t);
        f.setReadTimeout(t);

        this.http = builder
                .requestFactory(f) // Supplier gerekmiyor; doğrudan factory verilebilir
                .build();
    }

    /**
     * Gram başına USD fiyatı. API anahtarı yoksa ya da hata olursa fallback döner.
     */
    @Cacheable("goldPrice")
    public double getGoldPricePerGramUsd() {
        try {
            String p = provider == null ? "dummy" : provider.toLowerCase();
            double price;
            if (p.equals("metalsapi")) {
                price = fetchFromMetalsApi();
            } else if (p.equals("goldapi")) {
                price = fetchFromGoldApi();
            } else {
                price = fallback; // dummy
            }

            // başarı
            if (price > 0) recordSuccess(price);
            return price;

        } catch (Exception e) {
            recordFailure(e.getClass().getSimpleName() + ": " + e.getMessage());
            return fallback;
        }
    }

    // ---- Metals-API (opsiyonel) ----
    private double fetchFromMetalsApi() {
        if (apiKey == null || apiKey.isBlank()) {
            recordFailure("API key missing (MetalsAPI)");
            return fallback;
        }

        MetalsDto dto = http.mutate().baseUrl("https://metals-api.com")
                .build()
                .get()
                .uri(uri -> uri.path("/api/latest")
                        .queryParam("access_key", apiKey)
                        .queryParam("base", "USD")
                        .queryParam("symbols", "XAU")
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        (req, res) -> { throw new IllegalStateException("MetalsAPI " + res.getStatusCode()); })
                .body(MetalsDto.class);

        if (dto == null || dto.getRates() == null || !dto.getRates().containsKey("XAU")) return fallback;

        // base=USD olduğu için 1 / XAU → USD/oz
        double usdPerOz = 1.0 / dto.getRates().get("XAU");
        double usdPerGram = usdPerOz / 31.1034768;
        return round2(usdPerGram);
    }

    // ---- GoldAPI (opsiyonel) ----
    private double fetchFromGoldApi() {
        if (apiKey == null || apiKey.isBlank()) {
            recordFailure("API key missing (GoldAPI)");
            return fallback;
        }

        GoldDto dto = http.mutate().baseUrl("https://www.goldapi.io")
                .build()
                .get()
                .uri("/api/XAU/USD")
                .header("x-access-token", apiKey)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        (req, res) -> { throw new IllegalStateException("GoldAPI " + res.getStatusCode()); })
                .body(GoldDto.class);

        if (dto == null || dto.getPrice() <= 0) return fallback;

        double usdPerGram = dto.getPrice() / 31.1034768; // GoldAPI: USD/oz
        return round2(usdPerGram);
    }

    // ---- Health helpers ----
    private void recordSuccess(double price) {
        this.lastSuccessfulUsdPerGram = price;
        this.lastSuccessAt = Instant.now();
        this.lastErrorMessage = null;
    }

    private void recordFailure(String msg) {
        this.lastErrorMessage = msg;
        // lastSuccessAt / lastSuccessfulUsdPerGram korunur
    }

    public Instant getLastSuccessAt() { return lastSuccessAt; }
    public Double getLastSuccessfulUsdPerGram() { return lastSuccessfulUsdPerGram; }
    public String getLastErrorMessage() { return lastErrorMessage; }

    public boolean isFresh(Duration maxAge) {
        Instant last = this.lastSuccessAt;
        return last != null && Duration.between(last, Instant.now()).compareTo(maxAge) <= 0;
    }

    // ---- DTO'lar (POJO) ----
    private static class MetalsDto {
        private Map<String, Double> rates;
        public Map<String, Double> getRates() { return rates; }
        public void setRates(Map<String, Double> rates) { this.rates = rates; }
    }

    private static class GoldDto {
        private double price; // USD per troy ounce
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }

    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}
