package vn.zaloppay.couponservice.data.cache.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.EqualJitterDelay;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

/**
 * Cache configuration for setting up cache providers and related beans.
 */
@Configuration
public class CacheConfiguration {

    @Value("${cache.redis.url}")
    private String redisUrl;

    @Value("${cache.redis.password}")
    private String redisPassword;

    @Value("${cache.redis.min-idle}")
    private int redisMinIdle;

    @Value("${cache.redis.max-active}")
    private int redisMaxActive;

    @Value("${cache.redis.timeout}")
    private int redisTimeout;

    @Value("${cache.redis.retry-attempts}")
    private int redisRetryAttempts;

    @Value("${cache.redis.retry-delay-min}")
    private int redisRetryDelayMin;

    @Value("${cache.redis.retry-delay-max}")
    private int redisRetryDelayMax;

    @Value("${cache.redis.reconnection-delay-min}")
    private int redisReconnectionDelayMin;

    @Value("${cache.redis.reconnection-delay-max}")
    private int redisReconnectionDelayMax;

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
                .setRetryDelay(new EqualJitterDelay(Duration.ofMillis(redisRetryDelayMin), Duration.ofMillis(redisRetryDelayMax)))
                .setReconnectionDelay(new EqualJitterDelay(Duration.ofMillis(redisReconnectionDelayMin), Duration.ofMillis(redisReconnectionDelayMax)));

        return Redisson.create(config);
    }
}