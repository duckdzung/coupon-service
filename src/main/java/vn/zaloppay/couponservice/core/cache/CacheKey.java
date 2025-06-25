package vn.zaloppay.couponservice.core.cache;

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
    public static final String AVAILABLE_COUPONS_PREFIX = "available_coupons";
    public static final String ALL_COUPONS_PREFIX = "all_coupons";
    
    /**
     * Generate cache key for coupon by code
     * Format: coupon:code:{code}
     */
    public static String couponByCode(String code) {
        return String.join(SEPARATOR, COUPON_PREFIX, "code", code);
    }
    
    /**
     * Generate cache key for available coupons with filters
     * Format: available_coupons:{orderAmount}:{discountType}:{page}:{size}:{sortBy}:{sortDirection}
     */
    public static String availableCoupons(String orderAmount, String discountType, 
                                        int page, int size, String sortBy, String sortDirection) {
        return String.join(SEPARATOR, 
            AVAILABLE_COUPONS_PREFIX, 
            orderAmount, 
            discountType != null ? discountType : "null",
            String.valueOf(page),
            String.valueOf(size),
            sortBy != null ? sortBy : "null",
            sortDirection != null ? sortDirection : "null"
        );
    }
    
    /**
     * Generate cache key for all coupons with filters
     * Format: all_coupons:{discountType}:{usageType}:{page}:{size}:{sortBy}:{sortDirection}
     */
    public static String allCoupons(String discountType, String usageType,
                                  int page, int size, String sortBy, String sortDirection) {
        return String.join(SEPARATOR,
            ALL_COUPONS_PREFIX,
            discountType != null ? discountType : "null",
            usageType != null ? usageType : "null",
            String.valueOf(page),
            String.valueOf(size),
            sortBy != null ? sortBy : "null",
            sortDirection != null ? sortDirection : "null"
        );
    }
    
    /**
     * Pattern to match all available coupons cache entries
     */
    public static String availableCouponsPattern() {
        return AVAILABLE_COUPONS_PREFIX + "*";
    }
    
    /**
     * Pattern to match all coupons cache entries
     */
    public static String allCouponsPattern() {
        return ALL_COUPONS_PREFIX + "*";
    }
    
    /**
     * Generate lock key for coupon operations
     * Format: coupon:{code}
     */
    public static String couponLockKey(String code) {
        return String.join(SEPARATOR, COUPON_PREFIX, code);
    }
} 