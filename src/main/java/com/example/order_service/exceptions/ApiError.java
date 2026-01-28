package com.example.order_service.exceptions;

import java.time.Instant;

public class ApiError {
    private final int status;
    private final String message;
    private final String errorCode;
    private final Instant timestamp;

    public ApiError(int status, String message, String errorCode) {
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
    }


    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
