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

    private static final int LIMIT = 120;         // Max 120 requests per minute per IP
    private static final long WINDOW_MS = 60_000; // 1-minute window in milliseconds

    private static class Counter {
        int count;
        long windowStart;
    }

    private final Map<String, Counter> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        // Determine client IP (supporting proxies via X-Forwarded-For)
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null) ip = req.getRemoteAddr();

        // Get or create request counter for this IP
        Counter counter = buckets.computeIfAbsent(ip, k -> {
            Counter c = new Counter();
            c.count = 0;
            c.windowStart = Instant.now().toEpochMilli();
            return c;
        });

        long now = Instant.now().toEpochMilli();

        synchronized (counter) {
            // Reset counter if the time window has passed
            if (now - counter.windowStart > WINDOW_MS) {
                counter.windowStart = now;
                counter.count = 0;
            }

            counter.count++;

            // Deny request if rate limit exceeded
            if (counter.count > LIMIT) {
                res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                res.setHeader("Retry-After", "60");
                res.getWriter().write("Too Many Requests");
                return;
            }
        }

        // Continue with the filter chain
        chain.doFilter(req, res);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip rate limiting for monitoring and documentation endpoints
        String path = request.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs");
    }
}
