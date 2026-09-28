package com.ridelink.drivervehicleservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * MongoDB configuration enabling entity auditing (@CreatedDate, @LastModifiedDate).
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
