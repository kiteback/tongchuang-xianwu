package com.tcxw.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductCreateRequest(
        @NotBlank(message = "商品标题不能为空")
        @Size(max = 100, message = "商品标题不能超过100个字符")
        String title,

        @Size(max = 2000, message = "商品描述不能超过2000个字符")
        String description,

        @NotNull(message = "商品价格不能为空")
        @DecimalMin(value = "0.01", message = "商品价格必须大于0")
        BigDecimal price,

        @NotBlank(message = "商品分类不能为空")
        @Size(max = 50, message = "商品分类不能超过50个字符")
        String category
) {
}
