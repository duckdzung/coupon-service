package vn.zaloppay.couponservice.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DatabaseInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Checking database schema...");
        
        try {
            if (!isTableExists("coupons")) {
                log.info("Creating coupons table...");
                executeSchemaScript();
                log.info("Database schema created successfully!");
            } else {
                log.info("Coupons table already exists.");
            }
        } catch (Exception e) {
            log.error("Failed to initialize database schema", e);
            throw e;
        }
    }

    private boolean isTableExists(String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                        "WHERE table_schema = DATABASE() AND table_name = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            log.warn("Error checking if table exists: {}", e.getMessage());
            return false;
        }
    }

    private void executeSchemaScript() throws Exception {
        ClassPathResource resource = new ClassPathResource("schema.sql");
        
        if (!resource.exists()) {
            log.warn("Schema script not found at classpath:schema.sql");
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            String script = reader.lines().collect(Collectors.joining("\n"));
            
            // Split by semicolon and execute each statement
            String[] statements = script.split(";");
            
            for (String statement : statements) {
                String trimmedStatement = statement.trim();
                if (!trimmedStatement.isEmpty()) {
                    log.debug("Executing SQL: {}", trimmedStatement);
                    jdbcTemplate.execute(trimmedStatement);
                }
            }
        }
    }
} 