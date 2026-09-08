package com.tcxw.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // 订单超时队列
    @Bean
    public Queue orderTimeoutQueue(){
        return QueueBuilder.durable("order.timeout.queue")
                .deadLetterExchange("order.dlx")
                .deadLetterRoutingKey("order.timeout")
                .build();
    }

    //订单死信交换机
    @Bean
    public DirectExchange orderDlx(){
        return new DirectExchange("order.dlx");
    }

    //订单超时处理队列
    @Bean
    public Queue orderTimeoutHandlerQueue(){
        return QueueBuilder.durable("order.timeout.handler.queue")
                .build();
    }

    //绑定死信交换机和超时处理队列
    @Bean
    public Binding orderTimeoutBinding(){
        return BindingBuilder
                .bind(orderTimeoutHandlerQueue())
                .to(orderDlx())
                .with("order.timeout");
    }
}