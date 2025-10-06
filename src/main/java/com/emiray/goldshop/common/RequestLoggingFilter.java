package com.emiray.goldshop.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();

        try {
            chain.doFilter(req, res);
        } finally {
            long duration = System.currentTimeMillis() - start;

            // Simple one-line log: method, path, status, duration, IP
            String ip = req.getHeader("X-Forwarded-For");
            if (ip == null) ip = req.getRemoteAddr();

            logger.info("{} {} -> {} ({} ms) ip={}",
                    req.getMethod(),
                    req.getRequestURI(),
                    res.getStatus(),
                    duration,
                    ip
            );
        }
    }
}
