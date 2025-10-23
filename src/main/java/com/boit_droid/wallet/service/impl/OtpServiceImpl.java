package com.boit_droid.wallet.service.impl;

import com.boit_droid.wallet.dto.response.CustomApiResponse;
import com.boit_droid.wallet.dto.response.OtpRequiredResponse;
import com.boit_droid.wallet.service.OtpService;
import com.boit_droid.wallet.service.NotificationService;
import com.boit_droid.wallet.service.OtpAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

	private final NotificationService notificationService;
	private final OtpAuditService otpAuditService;

	private static final long OTP_TTL_SECONDS = 300; // 5 minutes
	private static final int MAX_OTP_ATTEMPTS = 3;
	private static final int OTP_RATE_LIMIT_PER_HOUR = 10; // Max 10 OTP requests per hour per user
	private static final int IP_RATE_LIMIT_PER_HOUR = 50; // Max 50 OTP requests per hour per IP
	private static final long RATE_LIMIT_WINDOW_MILLIS = 3600000; // 1 hour
	private static final long SUSPICIOUS_ACTIVITY_THRESHOLD = 5; // 5 failed attempts in short time
	
	private final Map<String, OtpRecord> otpCache = new ConcurrentHashMap<>();
	private final Map<String, RateLimitInfo> userRateLimits = new ConcurrentHashMap<>();
	private final Map<String, RateLimitInfo> ipRateLimits = new ConcurrentHashMap<>();
	private final Map<String, SuspiciousActivityTracker> suspiciousActivityMap = new ConcurrentHashMap<>();
	private final SecureRandom secureRandom = new SecureRandom();

	@Override
	public String requestOtpForWallet(String walletId, String purpose, String userId) {
		cleanExpired();
		String code = generateCode(6);
		String cacheKey = cacheKey(walletId, purpose);
		OtpRecord record = new OtpRecord(code, Instant.now().plusSeconds(OTP_TTL_SECONDS), 0);
		otpCache.put(cacheKey, record);
		try {
			notificationService.sendOtpCode(userId, purpose, code);
			log.info("OTP generated for wallet {} purpose {}", walletId, purpose);
			return code;
		} catch (Exception e) {
			log.warn("Failed to send OTP notification for wallet {}: {}", walletId, e.getMessage());
			return code;
		}
	}

	@Override
	public boolean verifyOtpForWallet(String walletId, String purpose, String otpCode) {
		cleanExpired();
		OtpRecord record = otpCache.get(cacheKey(walletId, purpose));
		if (record == null) {
			return false;
		}
		if (record.expiresAt.isBefore(Instant.now())) {
			otpCache.remove(cacheKey(walletId, purpose));
			return false;
		}
		if (!record.code.equals(otpCode)) {
			record.attempts.incrementAndGet();
			return false;
		}
		otpCache.remove(cacheKey(walletId, purpose));
		return true;
	}

	@Override
	public String requestOtpForUser(String userId, String purpose) {
		cleanExpired();
		String code = generateCode(6);
		String cacheKey = userCacheKey(userId, purpose);
		OtpRecord record = new OtpRecord(code, Instant.now().plusSeconds(OTP_TTL_SECONDS), 0);
		otpCache.put(cacheKey, record);
		try {
			notificationService.sendOtpCode(userId, purpose, code);
			log.info("OTP generated for user {} purpose {} code is {}", userId, purpose, code);
			return code;
		} catch (Exception e) {
			log.warn("Failed to send OTP notification for user {}: {}", userId, e.getMessage());
			return code;
		}
	}

	@Override
	public boolean verifyOtpForUser(String userId, String purpose, String otpCode) {
		cleanExpired();
		OtpRecord record = otpCache.get(userCacheKey(userId, purpose));
		if (record == null) {
			return false;
		}
		if (record.expiresAt.isBefore(Instant.now())) {
			otpCache.remove(userCacheKey(userId, purpose));
			return false;
		}
		if (!record.code.equals(otpCode)) {
			record.attempts.incrementAndGet();
			return false;
		}
		otpCache.remove(userCacheKey(userId, purpose));
		return true;
	}

	@Override
	public CustomApiResponse<OtpRequiredResponse> createOtpRequiredResponse(String requestId, String message, String purpose) {
		List<String> channels = Arrays.asList("SMS", "EMAIL");
		
		OtpRequiredResponse otpData = new OtpRequiredResponse(
			purpose,
			message,
			channels,
			(int) OTP_TTL_SECONDS,
			"Resubmit the same request with the 'otp' field included"
		);
		
		return CustomApiResponse.otpRequired(message, requestId, otpData);
	}

	@Override
	public String requestOtpWithSecurity(String userId, String purpose, String clientIp, String requestId) {
		cleanExpired();
		cleanExpiredRateLimits();
		
		// Check rate limits
		if (isRateLimited(userId, clientIp)) {
			String rateLimitType = isUserRateLimited(userId) ? "USER" : "IP";
			int currentCount = getCurrentRateLimit(userId, clientIp);
			int limit = rateLimitType.equals("USER") ? OTP_RATE_LIMIT_PER_HOUR : IP_RATE_LIMIT_PER_HOUR;
			
			otpAuditService.logRateLimitExceeded(userId, clientIp, requestId, rateLimitType, currentCount, limit);
			throw new SecurityException("Rate limit exceeded for OTP generation");
		}
		
		// Check for suspicious activity
		if (isSuspiciousActivity(userId, clientIp)) {
			otpAuditService.logSuspiciousActivity(userId, clientIp, requestId, 
				"EXCESSIVE_OTP_REQUESTS", "HIGH", "BLOCKED");
			throw new SecurityException("Suspicious activity detected");
		}
		
		try {
			// Generate secure OTP
			String code = generateSecureCode(6);
			String cacheKey = userCacheKey(userId, purpose);
			OtpRecord record = new OtpRecord(code, Instant.now().plusSeconds(OTP_TTL_SECONDS), 0);
			otpCache.put(cacheKey, record);
			
			// Update rate limits
			updateRateLimit(userId, clientIp);
			
			// Send notification
			notificationService.sendOtpCode(userId, purpose, code);
			
			// Log successful generation
			otpAuditService.logOtpGeneration(userId, purpose, clientIp, requestId, true, null);
			
			log.info("Secure OTP generated for user {} purpose {} from IP {}", userId, purpose, clientIp);
			return code;
			
		} catch (Exception e) {
			otpAuditService.logOtpGeneration(userId, purpose, clientIp, requestId, false, e.getMessage());
			log.error("Failed to generate secure OTP for user {}: {}", userId, e.getMessage());
			throw new RuntimeException("Failed to generate OTP", e);
		}
	}

	@Override
	public boolean verifyOtpWithSecurity(String userId, String purpose, String otpCode, String clientIp, String requestId) {
		cleanExpired();
		
		String cacheKey = userCacheKey(userId, purpose);
		OtpRecord record = otpCache.get(cacheKey);
		
		if (record == null) {
			otpAuditService.logOtpVerification(userId, purpose, clientIp, requestId, 
				false, "OTP_NOT_FOUND", 0);
			return false;
		}
		
		if (record.expiresAt.isBefore(Instant.now())) {
			otpCache.remove(cacheKey);
			otpAuditService.logOtpVerification(userId, purpose, clientIp, requestId, 
				false, "OTP_EXPIRED", record.attempts.incrementAndGet());
			return false;
		}
		
		// Check max attempts
		if (record.attempts.incrementAndGet() >= MAX_OTP_ATTEMPTS) {
			otpCache.remove(cacheKey);
			otpAuditService.logOtpVerification(userId, purpose, clientIp, requestId, 
				false, "MAX_ATTEMPTS_EXCEEDED", record.attempts.incrementAndGet());
			
			// Track suspicious activity
			trackSuspiciousActivity(userId, clientIp);
			return false;
		}
		
		if (!record.code.equals(otpCode)) {
			record.attempts.incrementAndGet();
			
			// Track failed attempt
			trackSuspiciousActivity(userId, clientIp);
			
			otpAuditService.logOtpVerification(userId, purpose, clientIp, requestId, 
				false, "INVALID_OTP", record.attempts.incrementAndGet());
			return false;
		}
		
		// Successful verification
		otpCache.remove(cacheKey);
		otpAuditService.logOtpVerification(userId, purpose, clientIp, requestId, 
			true, null, record.attempts.incrementAndGet());
		
		log.info("OTP successfully verified for user {} purpose {} from IP {}", userId, purpose, clientIp);
		return true;
	}

	@Override
	public boolean isRateLimited(String userId, String clientIp) {
		return isUserRateLimited(userId) || isIpRateLimited(clientIp);
	}

	@Override
	public boolean isSuspiciousActivity(String userId, String clientIp) {
		SuspiciousActivityTracker tracker = suspiciousActivityMap.get(userId + ":" + clientIp);
		if (tracker == null) {
			return false;
		}
		
		// Check if there are too many failed attempts in a short time
		long recentFailures = tracker.getRecentFailures(Instant.now().minusSeconds(300)); // Last 5 minutes
		return recentFailures >= SUSPICIOUS_ACTIVITY_THRESHOLD;
	}

	private String cacheKey(String walletId, String purpose) {
		return walletId + "|" + purpose;
	}

	private String userCacheKey(String userId, String purpose) {
		return "USER:" + userId + "|" + purpose;
	}

	private String generateCode(int digits) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < digits; i++) {
			sb.append(secureRandom.nextInt(10));
		}
		return sb.toString();
	}

	private String generateSecureCode(int digits) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < digits; i++) {
			sb.append(secureRandom.nextInt(10));
		}
		return sb.toString();
	}

	private boolean isUserRateLimited(String userId) {
		RateLimitInfo rateLimitInfo = userRateLimits.get(userId);
		if (rateLimitInfo == null) {
			return false;
		}
		
		long currentTime = System.currentTimeMillis();
		if (currentTime - rateLimitInfo.windowStart > RATE_LIMIT_WINDOW_MILLIS) {
			rateLimitInfo.reset(currentTime);
			return false;
		}
		
		return rateLimitInfo.requestCount.get() >= OTP_RATE_LIMIT_PER_HOUR;
	}

	private boolean isIpRateLimited(String clientIp) {
		RateLimitInfo rateLimitInfo = ipRateLimits.get(clientIp);
		if (rateLimitInfo == null) {
			return false;
		}
		
		long currentTime = System.currentTimeMillis();
		if (currentTime - rateLimitInfo.windowStart > RATE_LIMIT_WINDOW_MILLIS) {
			rateLimitInfo.reset(currentTime);
			return false;
		}
		
		return rateLimitInfo.requestCount.get() >= IP_RATE_LIMIT_PER_HOUR;
	}

	private int getCurrentRateLimit(String userId, String clientIp) {
		RateLimitInfo userLimit = userRateLimits.get(userId);
		RateLimitInfo ipLimit = ipRateLimits.get(clientIp);
		
		int userCount = userLimit != null ? userLimit.requestCount.get() : 0;
		int ipCount = ipLimit != null ? ipLimit.requestCount.get() : 0;
		
		return Math.max(userCount, ipCount);
	}

	private void updateRateLimit(String userId, String clientIp) {
		long currentTime = System.currentTimeMillis();
		
		// Update user rate limit
		userRateLimits.computeIfAbsent(userId, k -> new RateLimitInfo(currentTime))
			.requestCount.incrementAndGet();
		
		// Update IP rate limit
		ipRateLimits.computeIfAbsent(clientIp, k -> new RateLimitInfo(currentTime))
			.requestCount.incrementAndGet();
	}

	private void trackSuspiciousActivity(String userId, String clientIp) {
		String key = userId + ":" + clientIp;
		suspiciousActivityMap.computeIfAbsent(key, k -> new SuspiciousActivityTracker())
			.recordFailure(Instant.now());
	}

	private void cleanExpiredRateLimits() {
		long currentTime = System.currentTimeMillis();
		
		userRateLimits.entrySet().removeIf(entry -> 
			currentTime - entry.getValue().windowStart > RATE_LIMIT_WINDOW_MILLIS * 2);
		
		ipRateLimits.entrySet().removeIf(entry -> 
			currentTime - entry.getValue().windowStart > RATE_LIMIT_WINDOW_MILLIS * 2);
		
		// Clean old suspicious activity records
		suspiciousActivityMap.entrySet().removeIf(entry -> 
			entry.getValue().isExpired(Instant.now().minusSeconds(3600))); // Keep for 1 hour
	}

	private void cleanExpired() {
		Instant now = Instant.now();
		otpCache.entrySet().removeIf(e -> e.getValue().expiresAt.isBefore(now));
	}

	private static class OtpRecord {
		final String code;
		final Instant expiresAt;
		final AtomicInteger attempts;

		OtpRecord(String code, Instant expiresAt, int attempts) {
			this.code = code;
			this.expiresAt = expiresAt;
			this.attempts = new AtomicInteger(attempts);
		}
	}

	private static class RateLimitInfo {
		final AtomicInteger requestCount;
		long windowStart;

		RateLimitInfo(long windowStart) {
			this.requestCount = new AtomicInteger(0);
			this.windowStart = windowStart;
		}

		void reset(long newWindowStart) {
			this.requestCount.set(0);
			this.windowStart = newWindowStart;
		}
	}

	private static class SuspiciousActivityTracker {
		private final List<Instant> failureTimestamps = new java.util.concurrent.CopyOnWriteArrayList<>();

		void recordFailure(Instant timestamp) {
			failureTimestamps.add(timestamp);
			// Keep only recent failures (last hour)
			failureTimestamps.removeIf(ts -> ts.isBefore(timestamp.minusSeconds(3600)));
		}

		long getRecentFailures(Instant since) {
			return failureTimestamps.stream()
				.filter(ts -> ts.isAfter(since))
				.count();
		}

		boolean isExpired(Instant cutoff) {
			return failureTimestamps.isEmpty() || 
				   failureTimestamps.stream().allMatch(ts -> ts.isBefore(cutoff));
		}
	}
}






