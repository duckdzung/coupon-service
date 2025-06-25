package vn.zaloppay.couponservice.data.cache.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Cache configuration for setting up cache providers and related beans.
 */
@Configuration
public class CacheConfiguration {

    @Value("${cache.redis.url:redis://localhost:6379}")
    private String redisUrl;

    @Value("${cache.redis.password:}")
    private String redisPassword;

    @Value("${cache.redis.min-idle:5}")
    private int redisMinIdle;

    @Value("${cache.redis.max-active:20}")
    private int redisMaxActive;

    @Value("${cache.redis.timeout:3000}")
    private int redisTimeout;

    @Value("${cache.redis.retry-attempts:3}")
    private int redisRetryAttempts;

    @Value("${cache.redis.retry-interval:1500}")
    private int redisRetryInterval;

    /**
     * ObjectMapper bean for cache serialization/deserialization
     */
    @Bean
    @Primary
    public ObjectMapper cacheObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * RedissonClient bean for Redis operations
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(redisUrl)
                .setPassword(redisPassword.isEmpty() ? null : redisPassword)
                .setConnectionMinimumIdleSize(redisMinIdle)
                .setConnectionPoolSize(redisMaxActive)
                .setTimeout(redisTimeout)
                .setRetryAttempts(redisRetryAttempts)
                .setRetryInterval(redisRetryInterval);

        return Redisson.create(config);
    }
}