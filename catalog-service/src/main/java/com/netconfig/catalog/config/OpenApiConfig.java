package com.netconfig.catalog.config;

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
    public OpenAPI catalogServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NetConfig Catalog Service API")
                        .description("""
                                Product Catalog Service for the NetConfig CPQ system.
                                
                                This service manages the network hardware product catalog including:
                                - Racks (various sizes)
                                - Switches (Catalyst, Nexus series)
                                - Power Supply Units
                                - Cables and SFP modules
                                
                                Uses MongoDB for flexible product schema storage.
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
                                .url("http://localhost:8080")
                                .description("Local development server")
                ));
    }
}

