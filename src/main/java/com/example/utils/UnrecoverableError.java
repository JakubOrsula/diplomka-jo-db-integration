package com.example.utils;

public class UnrecoverableError extends Error{
    public UnrecoverableError() {
    }

    public UnrecoverableError(String message) {
        super(message);
    }

    public UnrecoverableError(String message, Throwable cause) {
        super(message, cause);
    }

    public UnrecoverableError(Throwable cause) {
        super(cause);
    }

    public UnrecoverableError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
