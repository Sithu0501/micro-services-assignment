package com.ridelink.drivervehicleservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 / Swagger documentation configuration for Driver & Vehicle Service.
 * Configures API metadata, server paths, and JWT Bearer security scheme.
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
        }
)
@SecurityScheme(
        name = "BearerAuth",
        description = "JWT Bearer token authentication. Format: 'Bearer <token>'",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {
}
