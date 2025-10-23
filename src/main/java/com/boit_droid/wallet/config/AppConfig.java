package com.boit_droid.wallet.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Autowired
    EnvironmentDetails environmentDetails;
    
    @Autowired
    RequestInterceptor requestInterceptor;
    
    @Autowired
    RateLimitingFilter rateLimitingFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor);
    }
    
    /**
     * Register rate limiting filter for API endpoints
     */
    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration() {
        FilterRegistrationBean<RateLimitingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(rateLimitingFilter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1); // Execute before other filters
        registration.setName("rateLimitingFilter");
        return registration;
    }
}
