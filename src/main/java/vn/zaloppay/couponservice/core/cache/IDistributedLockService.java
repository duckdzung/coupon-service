package vn.zaloppay.couponservice.core.cache;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Distributed lock service interface following Clean Architecture principles.
 * Core layer defines the contract, infrastructure layer provides implementation.
 */
public interface IDistributedLockService {
    
    /**
     * Execute operation with distributed lock
     * @param lockKey unique key for the lock
     * @param waitTime maximum time to wait for lock acquisition
     * @param leaseTime maximum time to hold the lock
     * @param operation operation to execute while holding the lock
     * @param <T> return type of operation
     * @return result of operation
     * @throws RuntimeException if lock cannot be acquired or operation fails
     */
    <T> T executeWithLock(String lockKey, Duration waitTime, Duration leaseTime, Supplier<T> operation);
    
    /**
     * Execute operation with distributed lock and retry mechanism
     * @param lockKey unique key for the lock
     * @param waitTime maximum time to wait for lock acquisition per attempt
     * @param leaseTime maximum time to hold the lock
     * @param maxRetries maximum number of retry attempts
     * @param retryDelay delay between retry attempts
     * @param operation operation to execute while holding the lock
     * @param <T> return type of operation
     * @return result of operation
     * @throws RuntimeException if lock cannot be acquired after all retries or operation fails
     */
    <T> T executeWithLockAndRetry(String lockKey, Duration waitTime, Duration leaseTime, 
                                  int maxRetries, Duration retryDelay, Supplier<T> operation);

} 