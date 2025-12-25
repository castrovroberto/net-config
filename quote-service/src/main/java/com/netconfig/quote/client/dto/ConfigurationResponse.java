package com.netconfig.quote.client.dto;

import java.util.List;

/**
 * DTO for configuration data received from the Configuration Service.
 */
public record ConfigurationResponse(
    String id,
    String name,
    String customerId,
    String rackSku,
    boolean validated,
    List<ConfigurationItemResponse> items
) {}

