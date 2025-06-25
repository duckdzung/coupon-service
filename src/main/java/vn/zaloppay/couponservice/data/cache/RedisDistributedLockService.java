package vn.zaloppay.couponservice.data.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import vn.zaloppay.couponservice.core.cache.IDistributedLockService;
import vn.zaloppay.couponservice.core.exceptions.BadRequestException;
import vn.zaloppay.couponservice.core.exceptions.ConflictException;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis implementation of distributed lock service using Redisson.
 * This belongs to infrastructure layer and implements core interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisDistributedLockService implements IDistributedLockService {
    
    private final RedissonClient redissonClient;
    
    private static final String LOCK_PREFIX = "lock:";
    
    @Override
    public <T> T executeWithLock(String lockKey, Duration waitTime, Duration leaseTime, Supplier<T> operation) {
        String fullLockKey = LOCK_PREFIX + lockKey;
        RLock lock = redissonClient.getLock(fullLockKey);
        
        try {
            boolean acquired = lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw new ConflictException("Resource is being processed by another request, please try again");
            }
            
            log.debug("Acquired distributed lock: {}", lockKey);
            return operation.get();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BadRequestException("Request was interrupted while waiting for lock");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Released distributed lock: {}", lockKey);
            }
        }
    }
    
    @Override
    public <T> T executeWithLockAndRetry(String lockKey, Duration waitTime, Duration leaseTime, 
                                         int maxRetries, Duration retryDelay, Supplier<T> operation) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries + 1; attempt++) {
            try {
                log.debug("Attempting to acquire lock: {} (attempt {}/{})", lockKey, attempt, maxRetries + 1);
                return executeWithLock(lockKey, waitTime, leaseTime, operation);
                
            } catch (ConflictException e) {
                lastException = e;
                
                if (attempt <= maxRetries) {
                    log.debug("Lock acquisition failed for attempt {}, retrying in {}ms...", 
                             attempt, retryDelay.toMillis());
                    
                    try {
                        Thread.sleep(retryDelay.toMillis());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new BadRequestException("Request was interrupted during retry delay");
                    }
                } else {
                    log.warn("Failed to acquire lock for key: {} after {} attempts", lockKey, maxRetries + 1);
                }
            }
        }
        
        // If we reach here, all attempts failed
        throw new BadRequestException("Failed to acquire lock after " + (maxRetries + 1) + 
                                    " attempts. Last error: " + lastException.getMessage());
    }

} 