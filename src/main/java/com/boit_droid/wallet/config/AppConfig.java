package com.boit_droid.wallet.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class AppConfig {

    @Autowired
    EnvironmentDetails environmentDetails;

    @Bean
    @Profile("dev")
    public EnvironmentDetails devEnv(){
        System.out.println("Dev environment active...");
        System.out.println(environmentDetails.toString());
        return environmentDetails;
    }

    @Bean
    @Profile("prod")
    public EnvironmentDetails prodEnv(){
        System.out.println("Prod environment active...");
        System.out.println(environmentDetails.toString());
        return environmentDetails;
    }
}
