package com.tcxw.dto;

import com.tcxw.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        String orderNo,
        Long buyerId,
        Long sellerId,
        Long productId,
        BigDecimal price,
        Integer status,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(), order.getOrderNo(), order.getBuyerId(), order.getSellerId(),
                order.getProductId(), order.getPrice(), order.getStatus(),
                order.getCreateTime(), order.getUpdateTime()
        );
    }
}
