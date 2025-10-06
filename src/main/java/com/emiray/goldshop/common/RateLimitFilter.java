package com.emiray.goldshop.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int LIMIT = 120;     // 60 s'de 120 istek
    private static final long WINDOW_MS = 60_000;

    private static class Counter { int count; long windowStart; }

    private final Map<String, Counter> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null) ip = req.getRemoteAddr();

        Counter c = buckets.computeIfAbsent(ip, k -> {
            Counter x = new Counter();
            x.count = 0; x.windowStart = Instant.now().toEpochMilli();
            return x;
        });

        long now = Instant.now().toEpochMilli();
        synchronized (c) {
            if (now - c.windowStart > WINDOW_MS) {
                c.windowStart = now;
                c.count = 0;
            }
            c.count++;
            if (c.count > LIMIT) {
                res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                res.setHeader("Retry-After", "60");
                res.getWriter().write("Too Many Requests");
                return;
            }
        }
        chain.doFilter(req, res);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Actuator ve static'leri hariç tut
        return path.startsWith("/actuator") || path.startsWith("/swagger") || path.startsWith("/v3/api-docs");
    }
}
