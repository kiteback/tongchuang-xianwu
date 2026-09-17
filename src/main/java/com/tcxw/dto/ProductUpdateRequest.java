package com.tcxw.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductUpdateRequest(
        @Size(min = 1, max = 100, message = "商品标题长度必须在1到100个字符之间")
        String title,

        @Size(max = 2000, message = "商品描述不能超过2000个字符")
        String description,

        @DecimalMin(value = "0.01", message = "商品价格必须大于0")
        BigDecimal price,

        @Size(min = 1, max = 50, message = "商品分类长度必须在1到50个字符之间")
        String category
) {
}
