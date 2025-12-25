package com.netconfig.configuration.service;

import com.netconfig.common.exception.ResourceNotFoundException;
import com.netconfig.common.exception.ValidationException;
import com.netconfig.configuration.client.CatalogClient;
import com.netconfig.configuration.client.dto.ProductResponse;
import com.netconfig.configuration.domain.ConfigurationItem;
import com.netconfig.configuration.domain.ConfigurationStatus;
import com.netconfig.configuration.domain.RackConfiguration;
import com.netconfig.configuration.repository.ConfigurationRepository;
import com.netconfig.configuration.validation.ConfigurationValidator;
import com.netconfig.configuration.validation.ValidationSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Service for managing rack configurations.
 */
@Service
public class ConfigurationService {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationService.class);

    private final ConfigurationRepository repository;
    private final ConfigurationValidator validator;
    private final CatalogClient catalogClient;

    public ConfigurationService(
            ConfigurationRepository repository,
            ConfigurationValidator validator,
            CatalogClient catalogClient) {
        this.repository = repository;
        this.validator = validator;
        this.catalogClient = catalogClient;
    }

    /**
     * Get all configurations.
     */
    public List<RackConfiguration> getAllConfigurations() {
        return repository.findAll();
    }

    /**
     * Get configurations for a specific customer.
     */
    public List<RackConfiguration> getConfigurationsByCustomer(String customerId) {
        return repository.findByCustomerId(customerId);
    }

    /**
     * Get a configuration by ID.
     */
    public RackConfiguration getConfiguration(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Configuration", id));
    }

    /**
     * Create a new configuration.
     */
    public RackConfiguration createConfiguration(RackConfiguration configuration) {
        configuration.setCreatedAt(Instant.now());
        configuration.setUpdatedAt(Instant.now());
        configuration.setStatus(ConfigurationStatus.DRAFT);
        configuration.setValidated(false);

        RackConfiguration saved = repository.save(configuration);
        log.info("Created configuration: {} for rack: {}", saved.getId(), saved.getRackSku());
        return saved;
    }

    /**
     * Update an existing configuration.
     */
    public RackConfiguration updateConfiguration(String id, RackConfiguration updates) {
        RackConfiguration existing = getConfiguration(id);

        if (updates.getName() != null) {
            existing.setName(updates.getName());
        }
        if (updates.getDescription() != null) {
            existing.setDescription(updates.getDescription());
        }
        if (updates.getRackSku() != null) {
            existing.setRackSku(updates.getRackSku());
        }
        if (updates.getItems() != null) {
            existing.setItems(updates.getItems());
        }

        // Mark as not validated since changes were made
        existing.setValidated(false);
        existing.setValidationErrors(List.of());
        existing.setStatus(ConfigurationStatus.DRAFT);
        existing.setUpdatedAt(Instant.now());

        RackConfiguration saved = repository.save(existing);
        log.info("Updated configuration: {}", saved.getId());
        return saved;
    }

    /**
     * Add a component to a configuration.
     */
    public RackConfiguration addComponent(String configurationId, ConfigurationItem item) {
        RackConfiguration configuration = getConfiguration(configurationId);

        // Validate that the product exists
        ProductResponse product = catalogClient.getProductBySku(item.getProductSku())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not found: " + item.getProductSku()));

        // Set product name from catalog
        item.setProductName(product.name());
        
        configuration.addItem(item);
        configuration.setValidated(false);
        configuration.setStatus(ConfigurationStatus.DRAFT);
        configuration.setUpdatedAt(Instant.now());

        RackConfiguration saved = repository.save(configuration);
        log.info("Added component {} to configuration {}", item.getProductSku(), configurationId);
        return saved;
    }

    /**
     * Remove a component from a configuration.
     */
    public RackConfiguration removeComponent(String configurationId, String itemId) {
        RackConfiguration configuration = getConfiguration(configurationId);
        
        configuration.removeItem(itemId);
        configuration.setValidated(false);
        configuration.setStatus(ConfigurationStatus.DRAFT);
        configuration.setUpdatedAt(Instant.now());

        RackConfiguration saved = repository.save(configuration);
        log.info("Removed component {} from configuration {}", itemId, configurationId);
        return saved;
    }

    /**
     * Update component quantity.
     */
    public RackConfiguration updateComponentQuantity(String configurationId, String itemId, int quantity) {
        RackConfiguration configuration = getConfiguration(configurationId);
        
        configuration.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));

        configuration.setValidated(false);
        configuration.setStatus(ConfigurationStatus.DRAFT);
        configuration.setUpdatedAt(Instant.now());

        RackConfiguration saved = repository.save(configuration);
        log.info("Updated quantity for component {} in configuration {}", itemId, configurationId);
        return saved;
    }

    /**
     * Validate a configuration.
     */
    public ValidationSummary validateConfiguration(String id) {
        RackConfiguration configuration = getConfiguration(id);
        
        ValidationSummary summary = validator.validate(configuration);
        
        // Update configuration with validation results
        configuration.setValidated(summary.valid());
        configuration.setValidationErrors(summary.getAllErrors());
        
        if (summary.valid()) {
            configuration.setStatus(ConfigurationStatus.VALIDATED);
        }
        
        configuration.setUpdatedAt(Instant.now());
        repository.save(configuration);
        
        log.info("Validated configuration {}: {}", id, summary.valid() ? "PASSED" : "FAILED");
        return summary;
    }

    /**
     * Validate a configuration and throw if invalid.
     */
    public RackConfiguration validateAndSave(String id) {
        ValidationSummary summary = validateConfiguration(id);
        
        if (!summary.valid()) {
            throw new ValidationException(
                    com.netconfig.common.dto.ValidationResult.failure(summary.getAllErrors().stream()
                            .map(e -> new com.netconfig.common.dto.ValidationResult.ValidationMessage(
                                    com.netconfig.common.dto.ValidationResult.ValidationMessage.Severity.ERROR, e))
                            .toList()));
        }
        
        return getConfiguration(id);
    }

    /**
     * Delete a configuration.
     */
    public void deleteConfiguration(String id) {
        RackConfiguration configuration = getConfiguration(id);
        repository.delete(configuration);
        log.info("Deleted configuration: {}", id);
    }

    /**
     * Clone a configuration.
     */
    public RackConfiguration cloneConfiguration(String id, String newName) {
        RackConfiguration original = getConfiguration(id);
        
        RackConfiguration clone = new RackConfiguration();
        clone.setName(newName != null ? newName : original.getName() + " (Copy)");
        clone.setDescription(original.getDescription());
        clone.setCustomerId(original.getCustomerId());
        clone.setRackSku(original.getRackSku());
        clone.setStatus(ConfigurationStatus.DRAFT);
        clone.setValidated(false);
        
        // Deep copy items
        for (ConfigurationItem originalItem : original.getItems()) {
            ConfigurationItem newItem = new ConfigurationItem(
                    originalItem.getProductSku(),
                    originalItem.getProductName(),
                    originalItem.getQuantity()
            );
            newItem.setRackPosition(originalItem.getRackPosition());
            clone.addItem(newItem);
        }
        
        RackConfiguration saved = repository.save(clone);
        log.info("Cloned configuration {} to new configuration {}", id, saved.getId());
        return saved;
    }

    /**
     * Get list of available validation rules.
     */
    public List<String> getValidationRules() {
        return validator.getRuleNames();
    }
}

