package com.netconfig.quote.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) documentation configuration.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI quoteServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NetConfig Quote Service API")
                        .description("""
                                Quote Generation Service for the NetConfig CPQ system.
                                
                                This service handles:
                                - Creating immutable quote records
                                - Async PDF generation (via RabbitMQ)
                                - Quote acceptance workflow
                                - Automatic expiration (30 days)
                                
                                **Architecture:**
                                - Uses PostgreSQL for ACID-compliant quote storage
                                - RabbitMQ for async PDF generation
                                - Scheduled job for quote expiration
                                
                                Quotes capture a point-in-time snapshot of configuration and pricing.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("NetConfig Team")
                                .email("support@netconfig.example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8083")
                                .description("Local development server")
                ));
    }
}

