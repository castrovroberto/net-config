package com.netconfig.configuration.client.dto;

import java.util.Map;

/**
 * DTO for product data received from the Catalog Service.
 */
public record ProductResponse(
    String id,
    String sku,
    String name,
    String type,
    double basePrice,
    Map<String, Object> attributes,
    Map<String, Object> compatibilityRules
) {
    /**
     * Get a typed attribute value.
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value == null) {
            return null;
        }
        if (type == Integer.class && value instanceof Number) {
            return (T) Integer.valueOf(((Number) value).intValue());
        }
        if (type == Double.class && value instanceof Number) {
            return (T) Double.valueOf(((Number) value).doubleValue());
        }
        if (type == Boolean.class && value instanceof Boolean) {
            return (T) value;
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
        Integer units = getAttribute("rack_units", Integer.class);
        // Some products like vertical-mount PSUs have 0 rack units
        return units != null ? units : 0;
    }

    public Integer getTotalRackUnits() {
        // For racks, this is the total capacity
        return getAttribute("units", Integer.class);
    }

    public boolean requiresPower() {
        Boolean requires = getAttribute("requires_power", Boolean.class);
        // Switches typically require power
        return requires != null ? requires : "SWITCH".equals(type);
    }
}

