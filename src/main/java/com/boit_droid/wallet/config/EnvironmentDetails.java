package com.boit_droid.wallet.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class EnvironmentDetails {

    @Value("${app.server.name}")
    private String appServerName;

    @Value("${app.server.detail}")
    private String appServerDetail;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    public EnvironmentDetails() {
        System.out.println(appServerName + " running...");
    }
}
