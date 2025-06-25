package vn.zaloppay.couponservice.core.cache;

import java.time.Duration;
import java.util.Optional;

/**
 * Cache service interface for caching operations.
 * This interface follows Clean Architecture principles by keeping cache operations abstract.
 */
public interface ICacheService {
    
    /**
     * Store a value in cache with specified TTL
     * @param key cache key
     * @param value value to cache
     * @param ttl time to live
     * @param <T> type of value
     */
    <T> void put(String key, T value, Duration ttl);
    
    /**
     * Retrieve a value from cache
     * @param key cache key
     * @param type class type for deserialization
     * @param <T> type of value
     * @return Optional containing the cached value if exists and not expired
     */
    <T> Optional<T> get(String key, Class<T> type);
    
    /**
     * Delete a specific cache entry
     * @param key cache key to delete
     */
    void delete(String key);
    
    /**
     * Delete cache entries matching a pattern
     * @param pattern pattern to match (supports wildcards like *)
     */
    void deletePattern(String pattern);
    
    /**
     * Check if a cache key exists and is not expired
     * @param key cache key to check
     * @return true if exists and not expired
     */
    boolean exists(String key);
    
    /**
     * Clear all cache entries
     */
    void clear();
    
} 