package com.tcxw.message;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class OrderMessage {

    private String eventId;

    private Long orderId;

    private Long userId;

    private String eventType;

    private OffsetDateTime occurredAt;

    public OrderMessage(){

    }

    public OrderMessage(Long orderId, Long userId, String eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.userId = userId;
        this.eventType = eventType;
        this.occurredAt = OffsetDateTime.now();
    }
}
