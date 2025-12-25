package com.netconfig.configuration.controller;

import com.netconfig.common.dto.ApiResponse;
import com.netconfig.configuration.domain.ConfigurationItem;
import com.netconfig.configuration.domain.RackConfiguration;
import com.netconfig.configuration.service.ConfigurationService;
import com.netconfig.configuration.validation.ValidationSummary;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for rack configuration operations.
 */
@RestController
@RequestMapping("/api/v1/configurations")
public class ConfigurationController {

    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * Get all configurations.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RackConfiguration>>> getAllConfigurations(
            @RequestParam(required = false) String customerId) {
        List<RackConfiguration> configurations = (customerId != null)
                ? configurationService.getConfigurationsByCustomer(customerId)
                : configurationService.getAllConfigurations();
        return ResponseEntity.ok(ApiResponse.success(configurations));
    }

    /**
     * Get a configuration by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RackConfiguration>> getConfiguration(@PathVariable String id) {
        RackConfiguration configuration = configurationService.getConfiguration(id);
        return ResponseEntity.ok(ApiResponse.success(configuration));
    }

    /**
     * Create a new configuration.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RackConfiguration>> createConfiguration(
            @Valid @RequestBody RackConfiguration configuration) {
        RackConfiguration created = configurationService.createConfiguration(configuration);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Configuration created successfully"));
    }

    /**
     * Update a configuration.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RackConfiguration>> updateConfiguration(
            @PathVariable String id,
            @Valid @RequestBody RackConfiguration configuration) {
        RackConfiguration updated = configurationService.updateConfiguration(id, configuration);
        return ResponseEntity.ok(ApiResponse.success(updated, "Configuration updated successfully"));
    }

    /**
     * Delete a configuration.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConfiguration(@PathVariable String id) {
        configurationService.deleteConfiguration(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Configuration deleted successfully"));
    }

    /**
     * Add a component to a configuration.
     */
    @PostMapping("/{id}/components")
    public ResponseEntity<ApiResponse<RackConfiguration>> addComponent(
            @PathVariable String id,
            @Valid @RequestBody ConfigurationItem item) {
        RackConfiguration updated = configurationService.addComponent(id, item);
        return ResponseEntity.ok(ApiResponse.success(updated, "Component added successfully"));
    }

    /**
     * Remove a component from a configuration.
     */
    @DeleteMapping("/{id}/components/{itemId}")
    public ResponseEntity<ApiResponse<RackConfiguration>> removeComponent(
            @PathVariable String id,
            @PathVariable String itemId) {
        RackConfiguration updated = configurationService.removeComponent(id, itemId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Component removed successfully"));
    }

    /**
     * Update component quantity.
     */
    @PatchMapping("/{id}/components/{itemId}")
    public ResponseEntity<ApiResponse<RackConfiguration>> updateComponentQuantity(
            @PathVariable String id,
            @PathVariable String itemId,
            @RequestParam int quantity) {
        RackConfiguration updated = configurationService.updateComponentQuantity(id, itemId, quantity);
        return ResponseEntity.ok(ApiResponse.success(updated, "Component quantity updated"));
    }

    /**
     * Validate a configuration.
     */
    @PostMapping("/{id}/validate")
    public ResponseEntity<ApiResponse<ValidationSummary>> validateConfiguration(@PathVariable String id) {
        ValidationSummary summary = configurationService.validateConfiguration(id);
        String message = summary.valid() 
                ? "Configuration is valid" 
                : "Configuration validation failed";
        return ResponseEntity.ok(ApiResponse.success(summary, message));
    }

    /**
     * Clone a configuration.
     */
    @PostMapping("/{id}/clone")
    public ResponseEntity<ApiResponse<RackConfiguration>> cloneConfiguration(
            @PathVariable String id,
            @RequestParam(required = false) String newName) {
        RackConfiguration cloned = configurationService.cloneConfiguration(id, newName);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(cloned, "Configuration cloned successfully"));
    }

    /**
     * Get list of validation rules.
     */
    @GetMapping("/validation-rules")
    public ResponseEntity<ApiResponse<List<String>>> getValidationRules() {
        List<String> rules = configurationService.getValidationRules();
        return ResponseEntity.ok(ApiResponse.success(rules));
    }
}

