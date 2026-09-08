package com.tcxw.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import com.tcxw.message.OrderMessage;

@Component
public class OrderMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public OrderMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendOrderCreatedMessage(OrderMessage message) {
        rabbitTemplate.convertAndSend(
                "order.exchange",
                "order.created",
                message
        );
    }

    public void sendOrderTimeoutMessage(OrderMessage message){
        rabbitTemplate.convertAndSend(
                "",
                "order.timeout.queue",
                message,
                messagePostProcessor -> {
                    messagePostProcessor.getMessageProperties().setExpiration("30000");
                    return messagePostProcessor;
                }
        );
    }
}