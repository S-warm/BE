package com.swarm.dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 캡스톤 시연 단계: 인증/인가 강제 비활성화.
 * - CSRF: 비활성화 (브라우저 → API 직접 호출 허용)
 * - CORS: WebConfig#addCorsMappings 의 설정을 사용 (Customizer.withDefaults)
 * - 모든 요청 permitAll
 *
 * 운영 단계 진입 시: JWT 또는 OAuth2 도입 + RBAC 권한 매핑 필요.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // CORS 처리는 WebConfig 의 매핑을 따라간다.
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
