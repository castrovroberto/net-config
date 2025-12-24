package com.netconfig.pricing.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Result of a pricing calculation.
 */
public class PricingResult {

    private String configurationId;
    private List<PricingLineItem> lineItems = new ArrayList<>();
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal totalDiscount = BigDecimal.ZERO;
    private BigDecimal serviceAddOn = BigDecimal.ZERO;
    private BigDecimal grandTotal = BigDecimal.ZERO;
    private String currency = "USD";
    private List<String> appliedStrategies = new ArrayList<>();
    private List<String> discountDescriptions = new ArrayList<>();
    private Instant calculatedAt;

    public PricingResult() {
        this.calculatedAt = Instant.now();
    }

    public PricingResult(String configurationId) {
        this.configurationId = configurationId;
        this.calculatedAt = Instant.now();
    }

    // Getters and Setters
    public String getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
    }

    public List<PricingLineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<PricingLineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public BigDecimal getServiceAddOn() {
        return serviceAddOn;
    }

    public void setServiceAddOn(BigDecimal serviceAddOn) {
        this.serviceAddOn = serviceAddOn;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<String> getAppliedStrategies() {
        return appliedStrategies;
    }

    public void setAppliedStrategies(List<String> appliedStrategies) {
        this.appliedStrategies = appliedStrategies;
    }

    public List<String> getDiscountDescriptions() {
        return discountDescriptions;
    }

    public void setDiscountDescriptions(List<String> discountDescriptions) {
        this.discountDescriptions = discountDescriptions;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(Instant calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public void addAppliedStrategy(String strategyName) {
        this.appliedStrategies.add(strategyName);
    }

    public void addDiscountDescription(String description) {
        this.discountDescriptions.add(description);
    }

    public void recalculateTotals() {
        this.subtotal = lineItems.stream()
                .map(PricingLineItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.totalDiscount = lineItems.stream()
                .map(PricingLineItem::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.grandTotal = subtotal
                .subtract(totalDiscount)
                .add(serviceAddOn);
    }
}

