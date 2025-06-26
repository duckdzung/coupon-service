package vn.zaloppay.couponservice.data.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import vn.zaloppay.couponservice.core.cache.ICacheService;
import vn.zaloppay.couponservice.presenter.config.logging.Limer;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis implementation of cache service using Redisson client.
 * This implementation provides distributed caching capabilities.
 */
@Service
@Primary
@RequiredArgsConstructor
@Slf4j
@Limer(enabledLogLatency = true)
public class RedisCacheService implements ICacheService {
    
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    
    @Override
    public <T> void put(String key, T value, Duration ttl) {
        try {
            RBucket<String> bucket = redissonClient.getBucket(key);
            String jsonValue = objectMapper.writeValueAsString(value);
            bucket.set(jsonValue, ttl);
            log.debug("Cached value with key: {} and TTL: {}", key, ttl);
        } catch (Exception e) {
            log.error("Error caching value with key: {}", key, e);
        }
    }
    
    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            RBucket<String> bucket = redissonClient.getBucket(key);
            String jsonValue = bucket.get();
            if (jsonValue != null) {
                T value = objectMapper.readValue(jsonValue, type);
                log.debug("Cache hit for key: {}", key);
                return Optional.of(value);
            }
            log.debug("Cache miss for key: {}", key);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error retrieving cached value with key: {}", key, e);
            return Optional.empty();
        }
    }
    
    @Override
    public void delete(String key) {
        try {
            boolean deleted = redissonClient.getBucket(key).delete();
            if (deleted) {
                log.debug("Deleted cache with key: {}", key);
            } else {
                log.debug("Cache key not found for deletion: {}", key);
            }
        } catch (Exception e) {
            log.error("Error deleting cache with key: {}", key, e);
        }
    }
    
    @Override
    public void deletePattern(String pattern) {
        try {
            long deletedCount = redissonClient.getKeys().deleteByPattern(pattern);
            log.debug("Deleted {} caches with pattern: {}", deletedCount, pattern);
        } catch (Exception e) {
            log.error("Error deleting caches with pattern: {}", pattern, e);
        }
    }
    
    @Override
    public boolean exists(String key) {
        try {
            return redissonClient.getBucket(key).isExists();
        } catch (Exception e) {
            log.error("Error checking cache existence for key: {}", key, e);
            return false;
        }
    }
    
    @Override
    public void clear() {
        try {
            redissonClient.getKeys().flushall();
            log.debug("Cleared all cache entries");
        } catch (Exception e) {
            log.error("Error clearing all cache entries", e);
        }
    }
} 