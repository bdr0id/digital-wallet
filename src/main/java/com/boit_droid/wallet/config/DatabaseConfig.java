package com.boit_droid.wallet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;

/**
 * Database configuration class for different environments
 * Provides optimized connection pool settings for each profile
 */
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    /**
     * Development environment datasource configuration
     */
    @Bean
    @Profile("dev")
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariConfig devHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("WalletDevPool");
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(20000);
        config.setMaxLifetime(1200000);
        config.setLeakDetectionThreshold(60000);
        return config;
    }

    /**
     * Production environment datasource configuration
     */
    @Bean
    @Profile("prod")
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariConfig prodHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("WalletProdPool");
        config.setMaximumPoolSize(50);
        config.setMinimumIdle(10);
        config.setIdleTimeout(600000);
        config.setConnectionTimeout(30000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);
        config.setAutoCommit(false);
        return config;
    }

    /**
     * Test environment datasource configuration
     */
    @Bean
    @Profile("test")
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariConfig testHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("WalletTestPool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(10000);
        config.setMaxLifetime(600000);
        return config;
    }
}