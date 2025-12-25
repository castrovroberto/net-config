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
    private BigDecimal lineItemDiscount = BigDecimal.ZERO;  // Discounts on individual items
    private BigDecimal orderDiscount = BigDecimal.ZERO;     // Order-level discounts (bundle, partner, etc.)
    private BigDecimal totalDiscount = BigDecimal.ZERO;     // Combined total discount
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

    public BigDecimal getLineItemDiscount() {
        return lineItemDiscount;
    }

    public void setLineItemDiscount(BigDecimal lineItemDiscount) {
        this.lineItemDiscount = lineItemDiscount;
    }

    public BigDecimal getOrderDiscount() {
        return orderDiscount;
    }

    public void setOrderDiscount(BigDecimal orderDiscount) {
        this.orderDiscount = orderDiscount;
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

    /**
     * Add an order-level discount (bundle, partner, etc.).
     */
    public void addOrderDiscount(BigDecimal discount) {
        this.orderDiscount = this.orderDiscount.add(discount);
        recalculateTotals();
    }

    /**
     * Recalculate totals from line items and order discounts.
     */
    public void recalculateTotals() {
        this.subtotal = lineItems.stream()
                .map(PricingLineItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.lineItemDiscount = lineItems.stream()
                .map(PricingLineItem::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.totalDiscount = lineItemDiscount.add(orderDiscount);
        
        this.grandTotal = subtotal
                .subtract(totalDiscount)
                .add(serviceAddOn);
    }
}

