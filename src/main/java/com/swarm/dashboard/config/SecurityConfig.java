package com.swarm.dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 보호 완전 비활성화
                .cors(cors -> cors.disable()) // CORS 처리 비활성화
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // 모든 요청 무조건 통과
                )
        