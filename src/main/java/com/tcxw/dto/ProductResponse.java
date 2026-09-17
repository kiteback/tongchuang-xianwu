package com.tcxw.dto;

import com.tcxw.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        Long userId,
        String title,
        String description,
        BigDecimal price,
        String category,
        Integer status,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getUserId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getStatus(),
                product.getCreateTime(),
                product.getUpdateTime()
        );
    }
}
