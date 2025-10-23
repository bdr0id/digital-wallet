package com.boit_droid.wallet.util;

import java.util.UUID;

/**
 * Utility class for generating unique request IDs for tracking and debugging
 */
public class RequestIdGenerator {
    
    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();
    
    /**
     * Generate a new request ID and set it in the current thread
     * @return the generated request ID
     */
    public static String generateAndSet() {
        String requestId = UUID.randomUUID().toString();
        REQUEST_ID.set(requestId);
        return requestId;
    }
    
    /**
     * Get the current request ID for the thread
     * @return the current request ID or null if not set
     */
    public static String getCurrentRequestId() {
        return REQUEST_ID.get();
    }
    
    /**
     * Set a specific request ID for the current thread
     * @param requestId the request ID to set
     */
    public static void setRequestId(String requestId) {
        REQUEST_ID.set(requestId);
    }
    
    /**
     * Clear the request ID from the current thread
     */
    public static void clear() {
        REQUEST_ID.remove();
    }
    
    /**
     * Generate a simple request ID without setting it in thread local
     * @return a new UUID string
     */
    public static String generateRequestId() {
        return UUID.randomUUID().toString();
    }
}