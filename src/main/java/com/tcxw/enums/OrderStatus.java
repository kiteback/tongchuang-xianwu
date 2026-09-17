package com.tcxw.enums;

public enum OrderStatus {
    PENDING_PAYMENT(1),
    PAID(2),
    COMPLETED(3),
    CANCELLED(4);

    private final int code;

    OrderStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
