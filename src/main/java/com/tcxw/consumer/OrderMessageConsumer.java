package com.tcxw.consumer;

import com.tcxw.message.OrderMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderMessageConsumer {

    @RabbitListener(queues = "order.queue")
    public void receiveMessage(OrderMessage message) {
        System.out.println("收到订单消息：orderId=" + message.getOrderId()
                + ", userId=" + message.getUserId()
                + ", eventType=" + message.getEventType()
        );
    }
}