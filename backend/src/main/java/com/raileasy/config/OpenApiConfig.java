package com.raileasy.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI metadata for the RailEasy API.
 * Swagger UI: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI railEasyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RailEasy API")
                        .description("Train Ticket Booking - Foundation capstone REST API")
                        .version("v0.0.1")
                        .license(new License().name("Educational use")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT returned by /api/auth/login")));
    }
}
