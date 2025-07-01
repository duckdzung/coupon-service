package vn.zaloppay.couponservice.domain.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Centralized cache key management.
 * This class provides type-safe cache key generation and pattern matching.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CacheKey {
    
    private static final String SEPARATOR = ":";
    
    // Cache prefixes
    public static final String COUPON_PREFIX = "coupon";

    /**
     * Generate cache key for coupon by code
     * Format: coupon:code:{code}
     */
    public static String couponByCode(String code) {
        return String.join(SEPARATOR, COUPON_PREFIX, "code", code);
    }
    
    /**
     * Generate lock key for coupon operations
     * Format: coupon:{code}
     */
    public static String couponLockKey(String code) {
        return String.join(SEPARATOR, COUPON_PREFIX, code);
    }
} 