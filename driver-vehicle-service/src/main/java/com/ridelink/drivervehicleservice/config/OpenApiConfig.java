package com.ridelink.drivervehicleservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 / Swagger documentation configuration for Driver & Vehicle Service.
 * Configures API metadata, server paths, and JWT Bearer security scheme.
 * Global security requirement ensures all endpoints require BearerAuth in Swagger UI.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "RideLink Driver & Vehicle Service API",
                version = "1.0.0",
                description = "Production-grade RESTful microservice for Driver Operational Profiles, " +
                        "Vehicle Management, Availability, Location Tracking, and Dispatch Discovery " +
                        "in the RideLink ride-hailing backend platform. Exclusively manages the 'ridelink_driver_db' database.",
                contact = @Contact(
                        name = "RideLink Engineering Team - Member 2 (Driver & Vehicle Service)",
                        email = "drivers@ridelink.com"
                ),
                license = @License(
                        name = "Academic / Proprietary",
                        url = "https://ridelink.com/license"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8082", description = "Local Development Environment")
        },
        security = {
                @SecurityRequirement(name = "BearerAuth")
        }
)
@SecurityScheme(
        name = "BearerAuth",
        description = "JWT Bearer token authentication. Paste ONLY the token (no 'Bearer' prefix). Swagger adds it automatically.",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {

    /**
     * Configures global security so every endpoint in Swagger UI automatically
     * sends the Authorization: Bearer header once the user clicks Authorize.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement()
                        .addList("BearerAuth"));
    }
}

