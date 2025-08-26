package com.alotrabong.shared.kernel;

import java.util.Optional;

public class Result<T> {
    private final T value;
    private final String error;
    private final boolean success;
    
    private Result(T value, String error, boolean success) {
        this.value = value;
        this.error = error;
        this.success = success;
    }
    
    public static <T> Result<T> success(T value) {
        return new Result<>(value, null, true);
    }
    
    public static <T> Result<T> failure(String error) {
        return new Result<>(null, error, false);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public boolean isFailure() {
        return !success;
    }
    
    public Optional<T> getValue() {
        return Optional.ofNullable(value);
    }
    
    public String getError() {
        return error;
    }
}

