package com.tcxw.controller;

import com.tcxw.message.OrderMessage;
import com.tcxw.producer.OrderMessageProducer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RabbitMQTestController {

    private final OrderMessageProducer orderMessageProducer;

    public RabbitMQTestController(OrderMessageProducer orderMessageProducer) {
        this.orderMessageProducer = orderMessageProducer;
    }

    @GetMapping("/rabbitmq-test")
    public String sendMessage(@RequestParam Long orderId,
                              @RequestParam Long userId) {

        OrderMessage message = new OrderMessage(
                orderId,
                userId,
                "ORDER_CREATED"
        );

        orderMessageProducer.sendOrderCreatedMessage(message);
        return "消息发送成功";
    }
}