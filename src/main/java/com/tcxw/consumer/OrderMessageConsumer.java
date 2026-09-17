package com.tcxw.consumer;

import com.tcxw.config.RabbitMQConfig;
import com.tcxw.message.OrderMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderMessageConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void receiveMessage(OrderMessage message) {
        log.info("Received order event: eventId={}, orderId={}, userId={}, type={}",
                message.getEventId(), message.getOrderId(), message.getUserId(), message.getEventType());
    }
}
