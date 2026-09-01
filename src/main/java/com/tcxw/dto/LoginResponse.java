package com.tcxw.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private Long id;
    private String username;
    private String nickname;
    private String token;

}
