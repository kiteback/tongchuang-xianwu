package com.tcxw.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_QUEUE = "order.queue";
    public static final String ORDER_CREATED_KEY = "order.created";
    public static final String ORDER_TIMEOUT_QUEUE = "order.timeout.queue";
    public static final String ORDER_DLX = "order.dlx";
    public static final String ORDER_TIMEOUT_HANDLER_QUEUE = "order.timeout.handler.queue";
    public static final String ORDER_TIMEOUT_KEY = "order.timeout";
    public static final String ORDER_FAILED_EXCHANGE = "order.failed.exchange";
    public static final String ORDER_FAILED_QUEUE = "order.failed.queue";
    public static final String ORDER_FAILED_KEY = "order.failed";

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE)
                .deadLetterExchange(ORDER_FAILED_EXCHANGE)
                .deadLetterRoutingKey(ORDER_FAILED_KEY)
                .build();
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(ORDER_CREATED_KEY);
    }

    @Bean
    public Queue orderTimeoutQueue() {
        return QueueBuilder.durable(ORDER_TIMEOUT_QUEUE)
                .deadLetterExchange(ORDER_DLX)
                .deadLetterRoutingKey(ORDER_TIMEOUT_KEY)
                .build();
    }

    @Bean
    public DirectExchange orderDlx() {
        return new DirectExchange(ORDER_DLX);
    }

    @Bean
    public Queue orderTimeoutHandlerQueue() {
        return QueueBuilder.durable(ORDER_TIMEOUT_HANDLER_QUEUE)
                .deadLetterExchange(ORDER_FAILED_EXCHANGE)
                .deadLetterRoutingKey(ORDER_FAILED_KEY)
                .build();
    }

    @Bean
    public Binding orderTimeoutBinding() {
        return BindingBuilder.bind(orderTimeoutHandlerQueue()).to(orderDlx()).with(ORDER_TIMEOUT_KEY);
    }

    @Bean
    public DirectExchange orderFailedExchange() {
        return new DirectExchange(ORDER_FAILED_EXCHANGE);
    }

    @Bean
    public Queue orderFailedQueue() {
        return QueueBuilder.durable(ORDER_FAILED_QUEUE).build();
    }

    @Bean
    public Binding orderFailedBinding() {
        return BindingBuilder.bind(orderFailedQueue()).to(orderFailedExchange()).with(ORDER_FAILED_KEY);
    }
}
