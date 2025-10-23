package com.boit_droid.wallet.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.boit_droid.wallet.dto.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rate limiting filter to prevent abuse and ensure fair usage of API endpoints
 */
@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int DEFAULT_REQUESTS_PER_MINUTE = 60;
    private static final int SENSITIVE_REQUESTS_PER_MINUTE = 10;
    private static final int OTP_REQUESTS_PER_MINUTE = 5; // Very restrictive for OTP operations
    private static final long WINDOW_SIZE_MILLIS = 60_000; // 1 minute
    
    private final ConcurrentHashMap<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String clientId = getClientIdentifier(request);
        String endpoint = request.getRequestURI();
        int requestLimit = getRequestLimit(endpoint);
        
        if (isRateLimited(clientId, requestLimit)) {
            handleRateLimitExceeded(response, clientId, request.getRequestURI());
            return;
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Get client identifier for rate limiting (IP address for now)
     */
    private String getClientIdentifier(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Get request limit based on endpoint sensitivity
     */
    private int getRequestLimit(String endpoint) {
        // OTP-related endpoints with very restrictive limits
        if (endpoint.contains("/otp") || 
            endpoint.contains("otp") ||
            (endpoint.contains("/transfer") && endpoint.contains("POST")) ||
            (endpoint.contains("/pin") && endpoint.contains("PUT"))) {
            return OTP_REQUESTS_PER_MINUTE;
        }
        
        // Sensitive endpoints with lower limits
        if (endpoint.contains("/transfer") || 
            endpoint.contains("/topup") || 
            endpoint.contains("/pin") ||
            endpoint.contains("/kyc") ||
            endpoint.contains("/register") ||
            endpoint.contains("/status")) {
            return SENSITIVE_REQUESTS_PER_MINUTE;
        }
        
        return DEFAULT_REQUESTS_PER_MINUTE;
    }

    /**
     * Check if client has exceeded rate limit
     */
    private boolean isRateLimited(String clientId, int requestLimit) {
        long currentTime = System.currentTimeMillis();
        
        RateLimitInfo rateLimitInfo = rateLimitMap.computeIfAbsent(clientId, 
            k -> new RateLimitInfo(currentTime));
        
        synchronized (rateLimitInfo) {
            // Reset window if expired
            if (currentTime - rateLimitInfo.getWindowStart() >= WINDOW_SIZE_MILLIS) {
                rateLimitInfo.reset(currentTime);
            }
            
            // Check if limit exceeded
            if (rateLimitInfo.getRequestCount().get() >= requestLimit) {
                log.warn("Rate limit exceeded for client: {} - Current count: {}, Limit: {}", 
                        clientId, rateLimitInfo.getRequestCount().get(), requestLimit);
                return true;
            }
            
            // Increment request count
            rateLimitInfo.getRequestCount().incrementAndGet();
            return false;
        }
    }

    /**
     * Handle rate limit exceeded response
     */
    private void handleRateLimitExceeded(HttpServletResponse response, String clientId, String requestUri) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "RATE_LIMIT_EXCEEDED",
            "Too many requests. Please try again later.",
            null, // requestId not available in filter
            requestUri,
            HttpStatus.TOO_MANY_REQUESTS.value()
        );
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        
        log.warn("Rate limit exceeded response sent to client: {}", clientId);
    }

    /**
     * Rate limit information holder
     */
    private static class RateLimitInfo {
        private final AtomicInteger requestCount;
        private final AtomicLong windowStart;

        public RateLimitInfo(long windowStart) {
            this.requestCount = new AtomicInteger(0);
            this.windowStart = new AtomicLong(windowStart);
        }

        public void reset(long newWindowStart) {
            this.requestCount.set(0);
            this.windowStart.set(newWindowStart);
        }

        public AtomicInteger getRequestCount() {
            return requestCount;
        }

        public long getWindowStart() {
            return windowStart.get();
        }
    }
}