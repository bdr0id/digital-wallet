package com.boit_droid.wallet.config;

import com.boit_droid.wallet.util.RequestIdGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to generate and manage request IDs for tracking and debugging
 */
@Component
public class RequestInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestInterceptor.class);
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Check if request ID is provided in header, otherwise generate one
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = RequestIdGenerator.generateAndSet();
        } else {
            RequestIdGenerator.setRequestId(requestId);
        }
        
        // Add request ID to response header
        response.setHeader(REQUEST_ID_HEADER, requestId);
        
        // Add request ID to MDC for logging
        MDC.put("requestId", requestId);
        
        logger.info("Request started - ID: {} - Method: {} - URI: {}", 
                   requestId, request.getMethod(), request.getRequestURI());
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        String requestId = RequestIdGenerator.getCurrentRequestId();
        
        logger.info("Request completed - ID: {} - Status: {}", requestId, response.getStatus());
        
        // Clean up thread-local variables
        RequestIdGenerator.clear();
        MDC.clear();
    }
}