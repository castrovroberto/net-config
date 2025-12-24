package com.netconfig.quote.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for quote events.
 */
@Configuration
public class RabbitMQConfig {

    public static final String QUOTE_EXCHANGE = "quote.exchange";
    public static final String QUOTE_REQUESTED_QUEUE = "quote.requested.queue";
    public static final String QUOTE_READY_QUEUE = "quote.ready.queue";
    public static final String QUOTE_REQUESTED_ROUTING_KEY = "quote.requested";
    public static final String QUOTE_READY_ROUTING_KEY = "quote.ready";

    @Bean
    public TopicExchange quoteExchange() {
        return new TopicExchange(QUOTE_EXCHANGE);
    }

    @Bean
    public Queue quoteRequestedQueue() {
        return QueueBuilder.durable(QUOTE_REQUESTED_QUEUE)
                .withArgument("x-dead-letter-exchange", QUOTE_EXCHANGE + ".dlx")
                .build();
    }

    @Bean
    public Queue quoteReadyQueue() {
        return QueueBuilder.durable(QUOTE_READY_QUEUE).build();
    }

    @Bean
    public Binding quoteRequestedBinding(Queue quoteRequestedQueue, TopicExchange quoteExchange) {
        return BindingBuilder.bind(quoteRequestedQueue)
                .to(quoteExchange)
                .with(QUOTE_REQUESTED_ROUTING_KEY);
    }

    @Bean
    public Binding quoteReadyBinding(Queue quoteReadyQueue, TopicExchange quoteExchange) {
        return BindingBuilder.bind(quoteReadyQueue)
                .to(quoteExchange)
                .with(QUOTE_READY_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, 
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}

