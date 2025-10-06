package com.emiray.goldshop.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI goldShopApi() {
        return new OpenAPI().info(
                new Info()
                        .title("Gold Shop API")
                        .description("Products endpoint: price = (popularityScore + 1) * weight * goldPrice (USD/gram)")
                        .version("v1")
        );
    }
}
