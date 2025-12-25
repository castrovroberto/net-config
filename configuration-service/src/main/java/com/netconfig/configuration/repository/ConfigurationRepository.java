package com.netconfig.configuration.repository;

import com.netconfig.configuration.domain.ConfigurationStatus;
import com.netconfig.configuration.domain.RackConfiguration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoDB repository for RackConfiguration entities.
 */
@Repository
public interface ConfigurationRepository extends MongoRepository<RackConfiguration, String> {

    List<RackConfiguration> findByCustomerId(String customerId);

    List<RackConfiguration> findByStatus(ConfigurationStatus status);

    List<RackConfiguration> findByCustomerIdAndStatus(String customerId, ConfigurationStatus status);

    List<RackConfiguration> findByRackSku(String rackSku);
}

