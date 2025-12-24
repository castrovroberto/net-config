package com.netconfig.catalog.domain;

/**
 * Types of network hardware products.
 */
public enum ProductType {
    RACK("Server Rack"),
    SWITCH("Network Switch"),
    PSU("Power Supply Unit"),
    CABLE("Network Cable"),
    SFP_MODULE("SFP Transceiver Module"),
    ACCESSORY("Rack Accessory");

    private final String displayName;

    ProductType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

