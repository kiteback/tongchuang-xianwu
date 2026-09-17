package com.tcxw.exception;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(
        String code,
        String message,
        OffsetDateTime timestamp,
        String path,
        Map<String, String> fieldErrors
) {
    public static ErrorResponse of(String code, String message, String path) {
        return new ErrorResponse(code, message, OffsetDateTime.now(), path, Map.of());
    }
}
