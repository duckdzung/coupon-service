package vn.zaloppay.couponservice.app.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.zaloppay.couponservice.app.config.logging.Limer;
import vn.zaloppay.couponservice.app.model.response.ApiResponse;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/redis-data")
@RequiredArgsConstructor
@Slf4j
@Limer(enabledLogLatency = true, enabledLogInOut = true)
public class RedisDataController {

    private final RedissonClient redissonClient;
    
    private static final String LARGE_DATA_KEY_PREFIX = "large_data_";
    private static final int BYTES_IN_MB = 1024 * 1024;
    private static final int MAX_DATA_SIZE_MB = 500; // Maximum allowed size for safety
    
    /**
     * Helper method to suggest garbage collection and log memory usage
     */
    private void logMemoryUsage(String operation) {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double usedPercentage = (double) usedMemory / maxMemory * 100;
        
        log.debug("Memory usage after {}: Used: {:.2f} MB ({:.1f}% of max), Max: {:.2f} MB", 
                operation, 
                usedMemory / (1024.0 * 1024.0), 
                usedPercentage,
                maxMemory / (1024.0 * 1024.0));
        
        // Suggest GC if memory usage is high (above 80%)
        if (usedPercentage > 80) {
            log.warn("High memory usage detected ({}%). Suggesting garbage collection.", usedPercentage);
            System.gc(); // Note: This is just a suggestion to the JVM
        }
    }

    @PostMapping("/create-large-data/{sizeMB}")
    public ResponseEntity<ApiResponse> createLargeData(@PathVariable int sizeMB) {
        try {
            // Validate input size
            if (sizeMB <= 0) {
                return new ResponseEntity<>(
                    ApiResponse.error("Data size must be greater than 0 MB"),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            if (sizeMB > MAX_DATA_SIZE_MB) {
                return new ResponseEntity<>(
                    ApiResponse.error("Data size cannot exceed " + MAX_DATA_SIZE_MB + " MB"),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            String dataKey = LARGE_DATA_KEY_PREFIX + sizeMB + "mb";
            log.info("Creating {}MB data in Redis with key: {}", sizeMB, dataKey);
            
            // Log initial memory usage
            logMemoryUsage("create operation start");
            
            // Generate data of specified size with memory optimization
            int totalBytes = sizeMB * BYTES_IN_MB;
            
            // Pre-calculate pattern and its byte size to avoid repeated conversions
            String basePattern = "This is a test data pattern for Redis storage. Created at: " + LocalDateTime.now() + " | ";
            byte[] basePatternBytes = basePattern.getBytes(StandardCharsets.UTF_8);
            int basePatternSize = basePatternBytes.length;
            
            // Pre-allocate StringBuilder with exact capacity to avoid resizing
            StringBuilder largeData = new StringBuilder(totalBytes + 1024); // Small buffer for safety
            largeData.ensureCapacity(totalBytes);
            
            // Build data efficiently
            int currentSize = 0;
            int chunkNumber = 0;
            
            while (currentSize < totalBytes) {
                largeData.append(basePattern);
                currentSize += basePatternSize;
                
                if (currentSize < totalBytes) {
                    String chunkInfo = "Data chunk " + chunkNumber + " | ";
                    largeData.append(chunkInfo);
                    currentSize += chunkInfo.getBytes(StandardCharsets.UTF_8).length;
                    chunkNumber++;
                }
                
                // Safety check to prevent infinite loop
                if (chunkNumber > 1000000) {
                    break;
                }
            }
            
            // Fill remaining space with efficient padding
            int remaining = totalBytes - currentSize;
            if (remaining > 0) {
                char[] padding = new char[remaining];
                java.util.Arrays.fill(padding, 'x');
                largeData.append(padding);
            }
            
            // Get final data and calculate actual size
            String finalData = largeData.toString();
            int actualSizeBytes = finalData.length(); // Use length() instead of getBytes() for efficiency
            double actualSizeMB = (double) actualSizeBytes / BYTES_IN_MB;
            
            // Clear StringBuilder to help GC
            largeData.setLength(0);
            largeData = null;
            
            // Log memory usage before Redis operation
            logMemoryUsage("data generation complete");
            
            // Store in Redis with 1 hour TTL
            RBucket<String> bucket = redissonClient.getBucket(dataKey);
            bucket.set(finalData, Duration.ofHours(1));
            
            // Clear the data reference to help GC
            finalData = null;
            
            // Log memory usage after Redis operation
            logMemoryUsage("data stored in Redis");
            
            log.info("Successfully created large data in Redis. Actual size: {:.2f} MB ({} bytes)", 
                    actualSizeMB, actualSizeBytes);
            
            return new ResponseEntity<>(
                ApiResponse.success(
                    String.format("Large data created successfully. Key: %s, Size: %.2f MB (%d bytes). TTL: 1 hour", 
                            dataKey, actualSizeMB, actualSizeBytes),
                    "Large data stored in Redis"
                ), 
                HttpStatus.CREATED
            );
            
        } catch (Exception e) {
            log.error("Error creating large data in Redis", e);
            return new ResponseEntity<>(
                ApiResponse.error("Failed to create large data: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/get-large-data/{sizeMB}")
    public ResponseEntity<ApiResponse> getLargeData(@PathVariable int sizeMB) {
        try {
            // Validate input size
            if (sizeMB <= 0) {
                return new ResponseEntity<>(
                    ApiResponse.error("Data size must be greater than 0 MB"),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            String dataKey = LARGE_DATA_KEY_PREFIX + sizeMB + "mb";
            log.info("Retrieving large data from Redis with key: {}", dataKey);
            
            // Log initial memory usage
            logMemoryUsage("get operation start");
            
            RBucket<String> bucket = redissonClient.getBucket(dataKey);
            
            // Check if key exists first to avoid loading large data unnecessarily
            if (!bucket.isExists()) {
                log.warn("Large data not found in Redis with key: {}", dataKey);
                return new ResponseEntity<>(
                    ApiResponse.error(String.format("Large data (%dMB) not found. Please create it first using POST /api/v1/redis-data/create-large-data/%d", sizeMB, sizeMB)),
                    HttpStatus.NOT_FOUND
                );
            }
            
            // Get TTL information without loading the data
            long ttlInSeconds = bucket.remainTimeToLive();
            String ttlInfo = ttlInSeconds > 0 ? 
                String.format("%.2f minutes", ttlInSeconds / 60.0) : 
                "No TTL or expired";
            
            // For memory efficiency, get data size and preview without loading entire content
            String dataPreview = null;
            int dataSizeBytes = 0;
            double dataSizeMB = 0.0;
            
            try {
                // Load data only for size calculation and preview
                String data = bucket.get();
                if (data != null) {
                    dataSizeBytes = data.length(); // Use length() instead of getBytes() for efficiency
                    dataSizeMB = (double) dataSizeBytes / BYTES_IN_MB;
                    
                    // Get preview efficiently
                    int previewLength = Math.min(1000, data.length());
                    dataPreview = data.substring(0, previewLength);
                    
                    // Help GC by nullifying reference immediately
                    data = null;
                    
                    // Log memory usage after loading data
                    logMemoryUsage("data loaded from Redis");
                } else {
                    // This shouldn't happen since we checked exists, but handle gracefully
                    return new ResponseEntity<>(
                        ApiResponse.error("Data key exists but content is null"),
                        HttpStatus.INTERNAL_SERVER_ERROR
                    );
                }
            } catch (OutOfMemoryError e) {
                log.error("Out of memory while retrieving large data with key: {}", dataKey, e);
                return new ResponseEntity<>(
                    ApiResponse.error("Data is too large to retrieve. Consider using smaller data sizes."),
                    HttpStatus.INSUFFICIENT_STORAGE
                );
            }
            
            log.info("Successfully retrieved large data metadata from Redis. Size: {:.2f} MB ({} bytes), TTL: {}", 
                    dataSizeMB, dataSizeBytes, ttlInfo);
            
            // Return metadata about the data instead of the actual data to avoid overwhelming the response
            return new ResponseEntity<>(
                ApiResponse.success(
                    String.format("Large data retrieved successfully. Key: %s, Size: %.2f MB (%d bytes). TTL remaining: %s. Data preview: %s...", 
                            dataKey, dataSizeMB, dataSizeBytes, ttlInfo, dataPreview),
                    "Large data found in Redis"
                ),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            log.error("Error retrieving large data from Redis", e);
            return new ResponseEntity<>(
                ApiResponse.error("Failed to retrieve large data: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
} 