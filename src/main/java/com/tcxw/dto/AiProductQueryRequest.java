package com.tcxw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiProductQueryRequest(
        @NotBlank(message = "问题不能为空")
        @Size(max = 500, message = "问题不能超过500个字符")
        String question
) {
}
