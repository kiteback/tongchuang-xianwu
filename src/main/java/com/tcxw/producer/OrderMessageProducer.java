package com.tcxw.producer;

import com.tcxw.config.RabbitMQConfig;
import com.tcxw.message.OrderMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;

@Component
public class OrderMessageProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderMessageProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final Duration timeout;

    public OrderMessageProducer(RabbitTemplate rabbitTemplate,
                                @Value("${app.order.timeout:30m}") Duration timeout) {
        this.rabbitTemplate = rabbitTemplate;
        this.timeout = timeout;
    }

    public void sendAfterCommit(OrderMessage message) {
        Runnable sender = () -> {
            try {
                sendOrderCreatedMessage(message);
                sendOrderTimeoutMessage(message);
            } catch (RuntimeException exception) {
                log.error("Failed to publish order event {} for order {}", message.getEventId(),
                        message.getOrderId(), exception);
            }
        };
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sender.run();
                }
            });
        } else {
            sender.run();
        }
    }

    public void sendOrderCreatedMessage(OrderMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_CREATED_KEY,
                message
        );
    }

    public void sendOrderTimeoutMessage(OrderMessage message) {
        rabbitTemplate.convertAndSend(
                "",
                RabbitMQConfig.ORDER_TIMEOUT_QUEUE,
                message,
                amqpMessage -> {
                    amqpMessage.getMessageProperties().setExpiration(String.valueOf(timeout.toMillis()));
                    return amqpMessage;
                }
        );
    }
}
