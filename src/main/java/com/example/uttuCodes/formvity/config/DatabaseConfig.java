package com.example.uttuCodes.formvity.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class DatabaseConfig {

    private static final String LOCAL_JDBC = "jdbc:postgresql://localhost:5432/formvity";

    @Bean
    @Primary
    public DataSource dataSource() {
        String raw = DatabaseUrlParser.firstNonBlank(
                System.getenv("SPRING_DATASOURCE_URL"),
                System.getenv("DATABASE_URL"));

        if (raw == null) {
            log.warn("No DATABASE_URL / SPRING_DATASOURCE_URL set; using local default {}", LOCAL_JDBC);
            raw = LOCAL_JDBC;
        }

        DatabaseUrlParser.ConnectionDetails details = DatabaseUrlParser.parse(raw);

        String user = DatabaseUrlParser.firstNonBlank(
                System.getenv("SPRING_DATASOURCE_USERNAME"),
                details.username());
        String password = System.getenv("SPRING_DATASOURCE_PASSWORD");
        if (password == null || password.isBlank()) {
            password = details.password() != null ? details.password() : "";
        }

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(details.jdbcUrl());
        ds.setDriverClassName("org.postgresql.Driver");
        if (user != null && !user.isBlank()) {
            ds.setUsername(user);
        }
        ds.setPassword(password);

        log.info("DataSource configured: jdbcUrl={}", details.jdbcUrl());
        return ds;
    }
}
