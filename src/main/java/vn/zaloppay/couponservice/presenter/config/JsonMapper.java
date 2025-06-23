package vn.zaloppay.couponservice.presenter.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonMapper {
    
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert object to JSON: {}", e.getMessage());
            return obj.toString();
        }
    }
    
    public static String toJson(Object[] objs) {
        if (objs == null || objs.length == 0) {
            return "[]";
        }
        
        try {
            return objectMapper.writeValueAsString(objs);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert object array to JSON: {}", e.getMessage());
            return java.util.Arrays.toString(objs);
        }
    }
} 