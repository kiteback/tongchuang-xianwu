package com.tcxw.enums;

public enum UserStatus {
    DISABLED(0),
    ACTIVE(1);

    private final int code;

    UserStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
