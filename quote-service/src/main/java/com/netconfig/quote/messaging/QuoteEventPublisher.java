package com.netconfig.quote.messaging;

import com.netconfig.common.event.QuoteReadyEvent;
import com.netconfig.common.event.QuoteRequestedEvent;
import com.netconfig.quote.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes quote-related events to RabbitMQ.
 */
@Component
public class QuoteEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(QuoteEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public QuoteEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishQuoteRequested(QuoteRequestedEvent event) {
        log.info("Publishing QuoteRequestedEvent for quote: {}", event.quoteId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.QUOTE_EXCHANGE,
                RabbitMQConfig.QUOTE_REQUESTED_ROUTING_KEY,
                event
        );
    }

    public void publishQuoteReady(QuoteReadyEvent event) {
        log.info("Publishing QuoteReadyEvent for quote: {}", event.quoteId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.QUOTE_EXCHANGE,
                RabbitMQConfig.QUOTE_READY_ROUTING_KEY,
                event
        );
    }
}

