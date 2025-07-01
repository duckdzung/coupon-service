package vn.zaloppay.couponservice.app.config.rate_limit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rate limiting annotation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * Maximum requests allowed
     */
    int maxRequests() default 60;
    
    /**
     * Expiry time in seconds for the rate limit window
     */
    int expirySeconds() default 60;
} 