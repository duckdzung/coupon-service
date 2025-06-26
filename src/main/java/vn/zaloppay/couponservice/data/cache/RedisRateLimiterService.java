package vn.zaloppay.couponservice.data.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisRateLimiterService {

    private final RedissonClient redissonClient;

    private static final String RATE_LIMIT_SCRIPT = """
            local key = KEYS[1]
            local requests = tonumber(redis.call('GET', key) or '-1')
            local max_requests = tonumber(ARGV[1])
            local expiry = tonumber(ARGV[2])
            
            if (requests == -1) or (requests < max_requests) then
              redis.call('INCR', key)
              redis.call('EXPIRE', key, expiry)
              return false
            else
              return true
            end
            """;

    public boolean isRateLimited(String key, int maxRequests, int expirySeconds) {
        try {
            RScript script = redissonClient.getScript(StringCodec.INSTANCE);
            List<Object> keys = Collections.singletonList("rate_limit:" + key);

            Boolean result = script.eval(
                    RScript.Mode.READ_WRITE,
                    RATE_LIMIT_SCRIPT,
                    RScript.ReturnType.BOOLEAN,
                    keys,
                    String.valueOf(maxRequests),
                    String.valueOf(expirySeconds)
            );


            log.debug("Rate limit check for key: {}, max: {}, expiry: {}, result: {}", key, maxRequests, expirySeconds, result);
            return Boolean.TRUE.equals(result);

        } catch (Exception e) {
            log.error("Error checking rate limit for key: {}", key, e);
            return false;
        }
    }

} 