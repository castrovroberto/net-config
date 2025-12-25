package com.netconfig.pricing.controller;

import com.netconfig.common.dto.ApiResponse;
import com.netconfig.pricing.domain.PricingResult;
import com.netconfig.pricing.dto.PricingRequest;
import com.netconfig.pricing.service.PricingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for pricing operations.
 */
@RestController
@RequestMapping("/api/v1/pricing")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    /**
     * Calculate pricing for a configuration.
     */
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<PricingResult>> calculatePrice(
            @Valid @RequestBody PricingRequest request) {
        PricingResult result = pricingService.calculatePrice(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Pricing calculated successfully"));
    }

    /**
     * Quick pricing calculation with just configuration ID.
     */
    @GetMapping("/configurations/{configurationId}")
    public ResponseEntity<ApiResponse<PricingResult>> getConfigurationPrice(
            @PathVariable String configurationId,
            @RequestParam(required = false) String customerTier,
            @RequestParam(required = false, defaultValue = "false") boolean includeSupport,
            @RequestParam(required = false, defaultValue = "STANDARD") String supportTier) {
        
        Map<String, Object> options = new java.util.HashMap<>();
        if (includeSupport) {
            options.put("include_support", true);
            options.put("support_tier", supportTier);
        }
        
        PricingRequest request = new PricingRequest(
                configurationId,
                customerTier,
                null,
                null,
                options
        );
        
        PricingResult result = pricingService.calculatePrice(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Get list of active pricing strategies.
     */
    @GetMapping("/strategies")
    public ResponseEntity<ApiResponse<List<String>>> getStrategies() {
        List<String> strategies = pricingService.getActiveStrategies();
        return ResponseEntity.ok(ApiResponse.success(strategies));
    }

    /**
     * Health check for pricing service.
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "strategies", pricingService.getActiveStrategies().size()
        );
        return ResponseEntity.ok(ApiResponse.success(health));
    }
}

