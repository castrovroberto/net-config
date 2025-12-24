package com.netconfig.configuration.domain;

import java.util.UUID;

/**
 * Represents a single component in a rack configuration.
 */
public class ConfigurationItem {

    private String id;
    private String productSku;
    private String productName;
    private int quantity;
    private Integer rackPosition; // U position in rack (1-42 for a 42U rack)

    public ConfigurationItem() {
        this.id = UUID.randomUUID().toString();
        this.quantity = 1;
    }

    public ConfigurationItem(String productSku, String productName, int quantity) {
        this.id = UUID.randomUUID().toString();
        this.productSku = productSku;
        this.productName = productName;
        this.quantity = quantity;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Integer getRackPosition() {
        return rackPosition;
    }

    public void setRackPosition(Integer rackPosition) {
        this.rackPosition = rackPosition;
    }
}

