package com.tcxw.message;

import lombok.Data;

@Data
public class OrderMessage {

    private Long orderId;

    private Long userId;

    private String eventType;

    public OrderMessage(){

    }

    public OrderMessage(Long orderId, Long userId, String eventType) {

        this.orderId = orderId;
        this.userId = userId;
        this.eventType = eventType;
    }
}