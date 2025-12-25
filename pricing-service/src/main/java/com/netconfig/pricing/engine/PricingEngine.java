package com.netconfig.pricing.engine;

import com.netconfig.pricing.client.CatalogClient;
import com.netconfig.pricing.client.ConfigurationClient;
import com.netconfig.pricing.client.dto.ConfigurationItemResponse;
import com.netconfig.pricing.client.dto.ConfigurationResponse;
import com.netconfig.pricing.client.dto.ProductResponse;
import com.netconfig.pricing.domain.PricingContext;
import com.netconfig.pricing.domain.PricingLineItem;
import com.netconfig.pricing.domain.PricingResult;
import com.netconfig.pricing.dto.PricingRequest;
import com.netconfig.pricing.strategy.PricingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * Pricing engine that orchestrates pricing strategies.
 * Applies strategies in order to calculate final price.
 */
@Component
public class PricingEngine {

    private static final Logger log = LoggerFactory.getLogger(PricingEngine.class);

    private final List<PricingStrategy> strategies;
    private final CatalogClient catalogClient;
    private final ConfigurationClient configurationClient;

    public PricingEngine(
            List<PricingStrategy> strategies,
            CatalogClient catalogClient,
            ConfigurationClient configurationClient) {
        // Sort strategies by order
        this.strategies = strategies.stream()
                .sorted(Comparator.comparingInt(PricingStrategy::getOrder))
                .toList();
        this.catalogClient = catalogClient;
        this.configurationClient = configurationClient;

        log.info("Initialized PricingEngine with {} strategies: {}",
                strategies.size(),
                strategies.stream().map(PricingStrategy::getName).toList());
    }

    /**
     * Calculate pricing for a configuration.
     *
     * @param request The pricing request with configuration ID and options
     * @return Complete pricing result
     */
    public PricingResult calculatePrice(PricingRequest request) {
        log.info("Calculating price for configuration: {}", request.configurationId());

        // Fetch configuration
        ConfigurationResponse configuration = configurationClient.getConfiguration(request.configurationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Configuration not found: " + request.configurationId()));

        // Build pricing context
        PricingContext context = buildContext(configuration, request);

        // Execute strategies in order
        PricingResult result = new PricingResult(request.configurationId());

        for (PricingStrategy strategy : strategies) {
            try {
                log.debug("Applying strategy: {}", strategy.getName());
                result = strategy.apply(context, result);
            } catch (Exception e) {
                log.error("Strategy {} failed", strategy.getName(), e);
                // Continue with other strategies
            }
        }

        log.info("Pricing complete for {}: subtotal=${}, discount=${}, total=${}",
                request.configurationId(),
                result.getSubtotal(),
                result.getTotalDiscount(),
                result.getGrandTotal());

        return result;
    }

    /**
     * Build pricing context from configuration and request.
     */
    private PricingContext buildContext(ConfigurationResponse configuration, PricingRequest request) {
        List<PricingLineItem> lineItems = new ArrayList<>();

        // Fetch product prices and build line items
        for (ConfigurationItemResponse item : configuration.items()) {
            Optional<ProductResponse> product = catalogClient.getProductBySku(item.productSku());
            
            if (product.isPresent()) {
                ProductResponse p = product.get();
                PricingLineItem lineItem = new PricingLineItem(
                        p.sku(),
                        p.name(),
                        p.type(),
                        item.quantity(),
                        p.basePrice()
                );
                lineItems.add(lineItem);
            } else {
                log.warn("Product not found in catalog: {}", item.productSku());
            }
        }

        // Add rack if present
        if (configuration.rackSku() != null) {
            catalogClient.getProductBySku(configuration.rackSku())
                    .ifPresent(rack -> lineItems.add(new PricingLineItem(
                            rack.sku(),
                            rack.name(),
                            rack.type(),
                            1,
                            rack.basePrice()
                    )));
        }

        PricingContext context = new PricingContext(configuration.id(), lineItems);
        context.setCustomerId(configuration.customerId());
        context.setCustomerTier(request.customerTier());
        context.setOptions(request.options() != null ? request.options() : new HashMap<>());

        // Set rack utilization if available
        if (request.rackUnitsUsed() != null && request.rackCapacity() != null) {
            context.setRackUnitsUsed(request.rackUnitsUsed());
            context.setRackCapacity(request.rackCapacity());
        }

        return context;
    }

    /**
     * Get list of active pricing strategies.
     */
    public List<String> getStrategyNames() {
        return strategies.stream()
                .map(PricingStrategy::getName)
                .toList();
    }

    /**
     * Calculate price with pre-built context (for testing).
     */
    public PricingResult calculatePrice(PricingContext context) {
        PricingResult result = new PricingResult(context.getConfigurationId());

        for (PricingStrategy strategy : strategies) {
            result = strategy.apply(context, result);
        }

        return result;
    }
}

