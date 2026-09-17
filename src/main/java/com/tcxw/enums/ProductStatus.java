package com.tcxw.enums;

public enum ProductStatus {
    AVAILABLE(1),
    LOCKED(2),
    SOLD(3);

    private final int code;

    ProductStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
