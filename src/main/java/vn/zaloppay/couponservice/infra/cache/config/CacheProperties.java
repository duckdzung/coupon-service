package vn.zaloppay.couponservice.infra.cache.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "cache.redis")
public class CacheProperties {

    private String mode;
    private String password;
    private int minIdle;
    private int maxActive;
    private int timeout;
    private int retryAttempts;
    private int retryDelayMin;
    private int retryDelayMax;
    private int reconnectionDelayMin;
    private int reconnectionDelayMax;
    private Single single = new Single();
    private Cluster cluster = new Cluster();

    @Setter
    @Getter
    public static class Single {
        private String address;
        private int connectionPoolSize;
        private int connectionMinimumIdleSize;
    }

    @Setter
    @Getter
    public static class Cluster {
        private List<String> nodes;
        private int scanInterval;
        private int masterConnectionPoolSize;
        private int slaveConnectionPoolSize;
        private String readMode;
        private String subscriptionMode;
    }

}