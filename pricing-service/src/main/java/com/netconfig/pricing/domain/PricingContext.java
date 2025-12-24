package com.netconfig.pricing.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context object containing all information needed for pricing calculations.
 */
public class PricingContext {

    private String configurationId;
    private String customerId;
    private String customerTier; // STANDARD, PARTNER, ENTERPRISE
    private List<PricingLineItem> lineItems;
    private Map<String, Object> options = new HashMap<>();

    public PricingContext() {
    }

    public PricingContext(String configurationId, List<PricingLineItem> lineItems) {
        this.configurationId = configurationId;
        this.lineItems = lineItems;
    }

    // Getters and Setters
    public String getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerTier() {
        return customerTier;
    }

    public void setCustomerTier(String customerTier) {
        this.customerTier = customerTier;
    }

    public List<PricingLineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<PricingLineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    public boolean hasOption(String key) {
        return options.containsKey(key) && Boolean.TRUE.equals(options.get(key));
    }

    public int getSwitchCount() {
        return lineItems.stream()
                .filter(item -> "SWITCH".equals(item.getProductType()))
                .mapToInt(PricingLineItem::getQuantity)
                .sum();
    }
}

