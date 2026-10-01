/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.OpenAPIDefinition
 *  io.swagger.v3.oas.annotations.enums.SecuritySchemeType
 *  io.swagger.v3.oas.annotations.info.Contact
 *  io.swagger.v3.oas.annotations.info.Info
 *  io.swagger.v3.oas.annotations.info.License
 *  io.swagger.v3.oas.annotations.security.SecurityRequirement
 *  io.swagger.v3.oas.annotations.security.SecurityScheme
 *  io.swagger.v3.oas.annotations.servers.Server
 *  io.swagger.v3.oas.models.OpenAPI
 *  io.swagger.v3.oas.models.security.SecurityRequirement
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package com.ridelink.ridemanagement.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info=@Info(title="RideLink Ride Management Service API", version="1.0.0", description="Production-grade RESTful microservice for Ride Requests, Driver Assignment, Lifecycle State Operations, and Ride History in the RideLink ride-hailing backend platform. Exclusively manages the 'ridelink_ride_db' database.", contact=@Contact(name="RideLink Engineering Team - Member 3 (Ride Management Service)", email="rides@ridelink.com"), license=@License(name="Academic / Proprietary", url="https://ridelink.com/license")), servers={@Server(url="http://localhost:8083", description="Local Development Environment")}, security={@io.swagger.v3.oas.annotations.security.SecurityRequirement(name="BearerAuth")})
@SecurityScheme(name="BearerAuth", description="JWT Bearer token authentication. Paste ONLY the token (no 'Bearer' prefix). Swagger adds it automatically.", type=SecuritySchemeType.HTTP, bearerFormat="JWT", scheme="bearer")
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
    }
}

