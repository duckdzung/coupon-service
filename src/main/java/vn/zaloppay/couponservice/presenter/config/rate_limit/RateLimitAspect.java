package vn.zaloppay.couponservice.presenter.config.rate_limit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.zaloppay.couponservice.core.exceptions.TooManyRequestsException;
import vn.zaloppay.couponservice.data.cache.RedisRateLimiterService;

/**
 * Simple rate limiting aspect
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {
    
    private final RedisRateLimiterService rateLimiterService;
    
    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        
        String clientKey = getClientKey();
        int maxRequests = rateLimit.maxRequests();
        int expirySeconds = rateLimit.expirySeconds();
        
        if (rateLimiterService.isRateLimited(clientKey, maxRequests, expirySeconds)) {
            log.warn("Rate limit exceeded for key: {}, max: {}, expiry: {}s", clientKey, maxRequests, expirySeconds);
            throw new TooManyRequestsException("Too many requests. Please try again later.");
        }
        
        return joinPoint.proceed();
    }
    
    private String getClientKey() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String clientIp = getClientIp(request);
                String endpoint = request.getRequestURI();
                return clientIp + ":" + endpoint;
            }
        } catch (Exception e) {
            log.error("Error getting client key", e);
        }
        return "unknown";
    }
    
    private String getClientIp(HttpServletRequest request) {
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
} 