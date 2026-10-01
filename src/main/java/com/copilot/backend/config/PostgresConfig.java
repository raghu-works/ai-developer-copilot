package com.copilot.backend.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PostgresConfig {

    @Value("${spring.datasource.url}")
    private String mysqlUrl;

    @Value("${spring.datasource.username}")
    private String mysqlUsername;

    @Value("${spring.datasource.password}")
    private String mysqlPassword;

    @Value("${postgres.datasource.url}")
    private String postgresUrl;

    @Value("${postgres.datasource.username}")
    private String postgresUsername;

    @Value("${postgres.datasource.password}")
    private String postgresPassword;

    // MySQL - MAIN DATABASE
    @Primary
    @Bean(name = "mysqlDataSource")
    public DataSource mysqlDataSource() {

        return DataSourceBuilder.create()
                .url(mysqlUrl)
                .username(mysqlUsername)
                .password(mysqlPassword)
                .build();
    }

    // PostgreSQL - VECTOR DATABASE
    @Bean(name = "postgresDataSource")
    public DataSource postgresDataSource() {

        return DataSourceBuilder.create()
                .url(postgresUrl)
                .username(postgresUsername)
                .password(postgresPassword)
                .build();
    }

    // JdbcTemplate specifically for PostgreSQL
    @Bean
    public JdbcTemplate postgresJdbcTemplate(
            @Qualifier("postgresDataSource") DataSource postgresDataSource) {

        return new JdbcTemplate(postgresDataSource);
    }
}