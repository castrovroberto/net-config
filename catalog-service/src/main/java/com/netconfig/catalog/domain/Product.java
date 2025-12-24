package com.netconfig.catalog.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Product entity stored in MongoDB.
 * Uses flexible schema with attributes map for variable product properties.
 */
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sku;

    private String name;
    private String description;

    @Indexed
    private ProductType type;

    private BigDecimal basePrice;
    private String currency = "USD";

    /**
     * Flexible attributes map for product-specific properties.
     * Examples:
     * - Switch: { "ports": 24, "poe": true, "power_draw": 350, "throughput": "10Gbps" }
     * - PSU: { "capacity_watts": 1000, "efficiency": "80_PLUS_GOLD" }
     * - Rack: { "units": 42, "max_weight_kg": 500 }
     */
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * Compatibility rules for configuration validation.
     * Examples:
     * - Switch: { "min_rack_units": 1, "required_power": true }
     * - Cable: { "compatible_ports": ["SFP+", "QSFP28"] }
     */
    private Map<String, Object> compatibilityRules = new HashMap<>();

    private boolean active = true;
    private Instant createdAt;
    private Instant updatedAt;

    public Product() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProductType getType() {
        return type;
    }

    public void setType(ProductType type) {
        this.type = type;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getCompatibilityRules() {
        return compatibilityRules;
    }

    public void setCompatibilityRules(Map<String, Object> compatibilityRules) {
        this.compatibilityRules = compatibilityRules;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods for accessing typed attributes
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return (T) value;
        }
        // Handle numeric type conversions
        if (value instanceof Number number) {
            if (type == Integer.class) {
                return (T) Integer.valueOf(number.intValue());
            } else if (type == Long.class) {
                return (T) Long.valueOf(number.longValue());
            } else if (type == Double.class) {
                return (T) Double.valueOf(number.doubleValue());
            }
        }
        return (T) value;
    }

    public Integer getPowerDraw() {
        return getAttribute("power_draw", Integer.class);
    }

    public Integer getCapacityWatts() {
        return getAttribute("capacity_watts", Integer.class);
    }

    public Integer getRackUnits() {
        return getAttribute("rack_units", Integer.class);
    }

    public Integer getPorts() {
        return getAttribute("ports", Integer.class);
    }
}

