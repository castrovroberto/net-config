package com.netconfig.catalog.config;

import com.netconfig.catalog.domain.Product;
import com.netconfig.catalog.domain.ProductType;
import com.netconfig.catalog.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Loads sample product data on application startup.
 * Only active in 'dev' profile.
 */
@Configuration
@Profile("dev")
public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    CommandLineRunner loadSampleData(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() > 0) {
                log.info("Database already contains data, skipping sample data load");
                return;
            }

            log.info("Loading sample product data...");

            // === RACKS ===
            productRepository.save(createProduct(
                "RACK-42U-STD",
                "42U Standard Server Rack",
                "Enterprise-grade 42U server rack with cable management",
                ProductType.RACK,
                new BigDecimal("2499.99"),
                Map.of(
                    "units", 42,
                    "max_weight_kg", 1000,
                    "depth_mm", 1000,
                    "width_mm", 600,
                    "power_slots", 2
                ),
                Map.of("max_psu", 4)
            ));

            productRepository.save(createProduct(
                "RACK-24U-COMPACT",
                "24U Compact Server Rack",
                "Space-efficient 24U rack for smaller deployments",
                ProductType.RACK,
                new BigDecimal("1299.99"),
                Map.of(
                    "units", 24,
                    "max_weight_kg", 500,
                    "depth_mm", 800,
                    "width_mm", 600,
                    "power_slots", 2
                ),
                Map.of("max_psu", 2)
            ));

            // === SWITCHES ===
            productRepository.save(createProduct(
                "SW-CATALYST-9300-24",
                "Catalyst 9300 24-Port Switch",
                "Enterprise-class stackable switch with 24 ports",
                ProductType.SWITCH,
                new BigDecimal("4599.99"),
                Map.of(
                    "ports", 24,
                    "poe", true,
                    "poe_budget_watts", 715,
                    "power_draw", 350,
                    "throughput_gbps", 10,
                    "rack_units", 1,
                    "stackable", true
                ),
                Map.of(
                    "min_rack_units", 1,
                    "requires_power", true
                )
            ));

            productRepository.save(createProduct(
                "SW-CATALYST-9300-48",
                "Catalyst 9300 48-Port Switch",
                "Enterprise-class stackable switch with 48 ports",
                ProductType.SWITCH,
                new BigDecimal("7299.99"),
                Map.of(
                    "ports", 48,
                    "poe", true,
                    "poe_budget_watts", 980,
                    "power_draw", 450,
                    "throughput_gbps", 10,
                    "rack_units", 1,
                    "stackable", true
                ),
                Map.of(
                    "min_rack_units", 1,
                    "requires_power", true
                )
            ));

            productRepository.save(createProduct(
                "SW-NEXUS-9336C",
                "Nexus 9336C-FX2 Data Center Switch",
                "High-performance 36-port 100G data center switch",
                ProductType.SWITCH,
                new BigDecimal("24999.99"),
                Map.of(
                    "ports", 36,
                    "port_speed_gbps", 100,
                    "poe", false,
                    "power_draw", 650,
                    "throughput_tbps", 7.2,
                    "rack_units", 1,
                    "stackable", false
                ),
                Map.of(
                    "min_rack_units", 1,
                    "requires_power", true
                )
            ));

            productRepository.save(createProduct(
                "SW-MERAKI-MS250-48",
                "Meraki MS250-48 Cloud Managed Switch",
                "Cloud-managed 48-port Gigabit switch",
                ProductType.SWITCH,
                new BigDecimal("5899.99"),
                Map.of(
                    "ports", 48,
                    "poe", true,
                    "poe_budget_watts", 370,
                    "power_draw", 180,
                    "throughput_gbps", 1,
                    "rack_units", 1,
                    "cloud_managed", true
                ),
                Map.of(
                    "min_rack_units", 1,
                    "requires_power", true
                )
            ));

            // === PSUs ===
            productRepository.save(createProduct(
                "PSU-1000W-PLAT",
                "1000W Platinum Rack PDU",
                "High-efficiency 1000W power distribution unit",
                ProductType.PSU,
                new BigDecimal("599.99"),
                Map.of(
                    "capacity_watts", 1000,
                    "efficiency", "80_PLUS_PLATINUM",
                    "outlets", 8,
                    "voltage", 220,
                    "rack_units", 1,
                    "redundant", false
                ),
                Map.of()
            ));

            productRepository.save(createProduct(
                "PSU-2000W-TITANIUM",
                "2000W Titanium Rack PDU",
                "Enterprise 2000W power distribution unit with monitoring",
                ProductType.PSU,
                new BigDecimal("1299.99"),
                Map.of(
                    "capacity_watts", 2000,
                    "efficiency", "80_PLUS_TITANIUM",
                    "outlets", 16,
                    "voltage", 220,
                    "rack_units", 2,
                    "redundant", true,
                    "monitoring", true
                ),
                Map.of()
            ));

            productRepository.save(createProduct(
                "PSU-3000W-MODULAR",
                "3000W Modular PDU System",
                "Modular high-capacity power distribution for data centers",
                ProductType.PSU,
                new BigDecimal("2499.99"),
                Map.of(
                    "capacity_watts", 3000,
                    "efficiency", "80_PLUS_TITANIUM",
                    "outlets", 24,
                    "voltage", 220,
                    "rack_units", 0, // Vertical mount
                    "redundant", true,
                    "monitoring", true,
                    "modular", true
                ),
                Map.of()
            ));

            // === CABLES ===
            productRepository.save(createProduct(
                "CBL-DAC-10G-3M",
                "10G DAC Cable 3M",
                "Direct Attach Copper cable for 10G connections",
                ProductType.CABLE,
                new BigDecimal("49.99"),
                Map.of(
                    "type", "DAC",
                    "speed_gbps", 10,
                    "length_meters", 3,
                    "connector", "SFP+"
                ),
                Map.of(
                    "compatible_ports", java.util.List.of("SFP+", "SFP28")
                )
            ));

            productRepository.save(createProduct(
                "CBL-FIBER-LC-10M",
                "LC Fiber Patch Cable 10M",
                "OM4 multimode fiber patch cable with LC connectors",
                ProductType.CABLE,
                new BigDecimal("34.99"),
                Map.of(
                    "type", "FIBER",
                    "fiber_type", "OM4",
                    "speed_gbps", 100,
                    "length_meters", 10,
                    "connector", "LC"
                ),
                Map.of()
            ));

            // === SFP MODULES ===
            productRepository.save(createProduct(
                "SFP-10G-SR",
                "10GBASE-SR SFP+ Module",
                "10G short-range multimode fiber transceiver",
                ProductType.SFP_MODULE,
                new BigDecimal("149.99"),
                Map.of(
                    "speed_gbps", 10,
                    "type", "SR",
                    "wavelength_nm", 850,
                    "max_distance_m", 300,
                    "fiber_type", "MMF"
                ),
                Map.of(
                    "compatible_ports", java.util.List.of("SFP+")
                )
            ));

            productRepository.save(createProduct(
                "SFP-10G-LR",
                "10GBASE-LR SFP+ Module",
                "10G long-range single-mode fiber transceiver",
                ProductType.SFP_MODULE,
                new BigDecimal("299.99"),
                Map.of(
                    "speed_gbps", 10,
                    "type", "LR",
                    "wavelength_nm", 1310,
                    "max_distance_m", 10000,
                    "fiber_type", "SMF"
                ),
                Map.of(
                    "compatible_ports", java.util.List.of("SFP+")
                )
            ));

            productRepository.save(createProduct(
                "QSFP-100G-SR4",
                "100GBASE-SR4 QSFP28 Module",
                "100G short-range multimode fiber transceiver",
                ProductType.SFP_MODULE,
                new BigDecimal("599.99"),
                Map.of(
                    "speed_gbps", 100,
                    "type", "SR4",
                    "wavelength_nm", 850,
                    "max_distance_m", 100,
                    "fiber_type", "MMF",
                    "lanes", 4
                ),
                Map.of(
                    "compatible_ports", java.util.List.of("QSFP28", "QSFP+")
                )
            ));

            log.info("Sample product data loaded successfully ({} products)", 
                    productRepository.count());
        };
    }

    private Product createProduct(
            String sku,
            String name,
            String description,
            ProductType type,
            BigDecimal price,
            Map<String, Object> attributes,
            Map<String, Object> compatibilityRules) {
        
        Product product = new Product();
        product.setSku(sku);
        product.setName(name);
        product.setDescription(description);
        product.setType(type);
        product.setBasePrice(price);
        product.setAttributes(new java.util.HashMap<>(attributes));
        product.setCompatibilityRules(new java.util.HashMap<>(compatibilityRules));
        return product;
    }
}

