package com.netconfig.configuration.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user's rack configuration.
 * Contains selected components and their arrangement.
 */
@Document(collection = "configurations")
public class RackConfiguration {

    @Id
    private String id;
    
    private String name;
    private String description;
    private String customerId;
    
    // The base rack SKU
    private String rackSku;
    
    // List of components added to the configuration
    private List<ConfigurationItem> items = new ArrayList<>();
    
    // Configuration status
    private ConfigurationStatus status = ConfigurationStatus.DRAFT;
    
    // Validation state
    private boolean validated = false;
    private List<String> validationErrors = new ArrayList<>();
    
    private Instant createdAt;
    private Instant updatedAt;

    public RackConfiguration() {
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

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getRackSku() {
        return rackSku;
    }

    public void setRackSku(String rackSku) {
        this.rackSku = rackSku;
    }

    public List<ConfigurationItem> getItems() {
        return items;
    }

    public void setItems(List<ConfigurationItem> items) {
        this.items = items;
    }

    public ConfigurationStatus getStatus() {
        return status;
    }

    public void setStatus(ConfigurationStatus status) {
        this.status = status;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
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

    public void addItem(ConfigurationItem item) {
        this.items.add(item);
        this.validated = false;
        this.updatedAt = Instant.now();
    }

    public void removeItem(String itemId) {
        this.items.removeIf(item -> item.getId().equals(itemId));
        this.validated = false;
        this.updatedAt = Instant.now();
    }
}

