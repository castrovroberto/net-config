package com.netconfig.configuration.config;

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
    public OpenAPI configurationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NetConfig Configuration Service API")
                        .description("""
                                Configuration & Validation Service for the NetConfig CPQ system.
                                
                                This service handles:
                                - Creating rack configurations
                                - Adding/removing components
                                - Validation rules engine (Chain of Responsibility pattern):
                                  * Power budget validation
                                  * Rack capacity validation
                                  * Component compatibility
                                  * PSU redundancy warnings
                                
                                Uses MongoDB for flexible configuration storage.
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
                                .url("http://localhost:8081")
                                .description("Local development server")
                ));
    }
}

