package com.swarm.dashboard.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI uxSwarmOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UX-Swarm API")
                        .description("AI 기반 사용자 군집 UX 테스팅 솔루션 API 명세서")
                        .version("v1.0.0"));
    }
}