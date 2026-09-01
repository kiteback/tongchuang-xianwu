package com.tcxw.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;
}