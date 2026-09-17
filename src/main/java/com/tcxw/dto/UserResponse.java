package com.tcxw.dto;

import lombok.Data;
import com.tcxw.entity.User;

@Data
public class UserResponse {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer status;
    private String role;

    public static UserResponse from(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setStatus(user.getStatus());
        response.setRole(user.getRole());
        return response;
    }
}
