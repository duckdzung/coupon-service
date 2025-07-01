package vn.zaloppay.couponservice.infra.cache.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.config.SubscriptionMode;

import org.redisson.config.EqualJitterDelay;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@Slf4j
public class CacheConfiguration {

    private final CacheProperties cacheProperties;

    public CacheConfiguration(CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String mode = cacheProperties.getMode();
        
        log.info("Initializing Redis in {} mode", mode);
        
        try {
            if ("single".equalsIgnoreCase(mode)) {
                configureSingleServer(config);
            } else if ("cluster".equalsIgnoreCase(mode)) {
                configureClusterServers(config);
            } else {
                throw new IllegalArgumentException("Invalid Redis mode: " + mode + ". Supported modes: single, cluster");
            }

            RedissonClient client = Redisson.create(config);
            log.info("Redis connection established successfully in {} mode", mode);
            return client;
        } catch (Exception e) {
            log.error("Failed to create Redis connection in {} mode: {}", mode, e.getMessage());
            if ("cluster".equalsIgnoreCase(mode)) {
                log.warn("Cluster connection failed. Consider switching to single mode for development or check cluster configuration");
            }
            throw new RuntimeException("Redis connection failed", e);
        }
    }
    
    private void configureSingleServer(Config config) {
        log.info("Configuring Redis single server mode with address: {}", cacheProperties.getSingle().getAddress());
        
        config.useSingleServer()
                .setAddress(cacheProperties.getSingle().getAddress())
                .setPassword(cacheProperties.getPassword())
                .setConnectionMinimumIdleSize(cacheProperties.getSingle().getConnectionMinimumIdleSize())
                .setConnectionPoolSize(cacheProperties.getSingle().getConnectionPoolSize())
                .setTimeout(cacheProperties.getTimeout())
                .setRetryAttempts(cacheProperties.getRetryAttempts())
                .setRetryDelay(new EqualJitterDelay(Duration.ofMillis(cacheProperties.getRetryDelayMin()), Duration.ofMillis(cacheProperties.getRetryDelayMax())))
                .setReconnectionDelay(new EqualJitterDelay(Duration.ofMillis(cacheProperties.getReconnectionDelayMin()), Duration.ofMillis(cacheProperties.getReconnectionDelayMax())));
    }
    
    private void configureClusterServers(Config config) {
        List<String> nodes = cacheProperties.getCluster().getNodes();
        if (nodes == null || nodes.isEmpty()) {
            throw new IllegalArgumentException("Redis cluster nodes cannot be null or empty");
        }
        
        log.info("Configuring Redis cluster mode with {} nodes: {}", nodes.size(), nodes);

        config.useClusterServers()
                .addNodeAddress(nodes.toArray(new String[0]))
                .setPassword(cacheProperties.getPassword())
                .setMasterConnectionMinimumIdleSize(cacheProperties.getMinIdle())
                .setMasterConnectionPoolSize(cacheProperties.getCluster().getMasterConnectionPoolSize())
                .setSlaveConnectionMinimumIdleSize(cacheProperties.getMinIdle())
                .setSlaveConnectionPoolSize(cacheProperties.getCluster().getSlaveConnectionPoolSize())
                .setTimeout(cacheProperties.getTimeout())
                .setRetryAttempts(cacheProperties.getRetryAttempts())
                .setRetryDelay(new EqualJitterDelay(Duration.ofMillis(cacheProperties.getRetryDelayMin()), Duration.ofMillis(cacheProperties.getRetryDelayMax())))
                .setReconnectionDelay(new EqualJitterDelay(Duration.ofMillis(cacheProperties.getReconnectionDelayMin()), Duration.ofMillis(cacheProperties.getReconnectionDelayMax())))
                .setScanInterval(cacheProperties.getCluster().getScanInterval())
                .setReadMode(ReadMode.valueOf(cacheProperties.getCluster().getReadMode()))
                .setSubscriptionMode(SubscriptionMode.valueOf(cacheProperties.getCluster().getSubscriptionMode()));
    }

}