package com.netconfig.pricing.config;

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
    public OpenAPI pricingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NetConfig Pricing Service API")
                        .description("""
                                Pricing Engine for the NetConfig CPQ system.
                                
                                This service implements the Strategy Pattern for flexible pricing:
                                
                                **Pricing Strategies:**
                                - Base Price: Sum of all component prices
                                - Volume Discount: 10% off when > 5 switches
                                - Bundle Discount: 5% off when rack > 75% utilized
                                - Partner Discount: Tier-based discounts (5-15%)
                                - Support Add-on: 20% of subtotal for premium support
                                
                                All strategies are composable and executed in order.
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
                                .url("http://localhost:8082")
                                .description("Local development server")
                ));
    }
}

