package com.tcxw.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(max = 50, message = "昵称不能超过50个字符")
        String nickname,

        @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确")
        String phone,

        @Size(min = 8, max = 72, message = "密码长度必须在8到72个字符之间")
        String password,

        Integer status,
        String role
) {
}
