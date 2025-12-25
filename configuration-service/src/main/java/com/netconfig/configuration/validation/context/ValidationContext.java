package com.netconfig.configuration.validation.context;

import com.netconfig.configuration.client.dto.ProductResponse;
import com.netconfig.configuration.domain.ConfigurationItem;
import com.netconfig.configuration.domain.RackConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Context object containing all data needed for validation.
 * Pre-loads product information to avoid repeated service calls.
 */
public class ValidationContext {

    private final RackConfiguration configuration;
    private final ProductResponse rack;
    private final Map<String, ProductResponse> productsBySku;
    private final Map<String, List<ConfigurationItem>> itemsByType;

    public ValidationContext(
            RackConfiguration configuration,
            ProductResponse rack,
            Map<String, ProductResponse> productsBySku) {
        this.configuration = configuration;
        this.rack = rack;
        this.productsBySku = productsBySku;
        this.itemsByType = groupItemsByProductType();
    }

    private Map<String, List<ConfigurationItem>> groupItemsByProductType() {
        Map<String, List<ConfigurationItem>> grouped = new HashMap<>();
        
        for (ConfigurationItem item : configuration.getItems()) {
            ProductResponse product = productsBySku.get(item.getProductSku());
            if (product != null) {
                String type = product.type();
                grouped.computeIfAbsent(type, k -> new java.util.ArrayList<>()).add(item);
            }
        }
        
        return grouped;
    }

    public RackConfiguration getConfiguration() {
        return configuration;
    }

    public ProductResponse getRack() {
        return rack;
    }

    public Optional<ProductResponse> getProduct(String sku) {
        return Optional.ofNullable(productsBySku.get(sku));
    }

    public List<ConfigurationItem> getItemsByType(String type) {
        return itemsByType.getOrDefault(type, List.of());
    }

    public List<ConfigurationItem> getSwitches() {
        return getItemsByType("SWITCH");
    }

    public List<ConfigurationItem> getPsus() {
        return getItemsByType("PSU");
    }

    public List<ConfigurationItem> getAllItems() {
        return configuration.getItems();
    }

    /**
     * Calculate total power draw from all powered components.
     */
    public int getTotalPowerDraw() {
        return configuration.getItems().stream()
                .mapToInt(item -> {
                    ProductResponse product = productsBySku.get(item.getProductSku());
                    if (product != null && product.getPowerDraw() != null) {
                        return product.getPowerDraw() * item.getQuantity();
                    }
                    return 0;
                })
                .sum();
    }

    /**
     * Calculate total PSU capacity.
     */
    public int getTotalPsuCapacity() {
        return getPsus().stream()
                .mapToInt(item -> {
                    ProductResponse product = productsBySku.get(item.getProductSku());
                    if (product != null && product.getCapacityWatts() != null) {
                        return product.getCapacityWatts() * item.getQuantity();
                    }
                    return 0;
                })
                .sum();
    }

    /**
     * Calculate total rack units used by components.
     */
    public int getTotalRackUnitsUsed() {
        return configuration.getItems().stream()
                .mapToInt(item -> {
                    ProductResponse product = productsBySku.get(item.getProductSku());
                    if (product != null) {
                        return product.getRackUnits() * item.getQuantity();
                    }
                    return 0;
                })
                .sum();
    }

    /**
     * Get the rack's total capacity in units.
     */
    public int getRackCapacity() {
        return rack != null && rack.getTotalRackUnits() != null 
                ? rack.getTotalRackUnits() 
                : 0;
    }

    /**
     * Check if any powered components exist in the configuration.
     */
    public boolean hasPoweredComponents() {
        return configuration.getItems().stream()
                .anyMatch(item -> {
                    ProductResponse product = productsBySku.get(item.getProductSku());
                    return product != null && product.requiresPower();
                });
    }

    /**
     * Get count of a specific product type.
     */
    public int getCountByType(String type) {
        return getItemsByType(type).stream()
                .mapToInt(ConfigurationItem::getQuantity)
                .sum();
    }
}

