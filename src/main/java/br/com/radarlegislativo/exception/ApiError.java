package br.com.radarlegislativo.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public record ApiError(int status, String message, List<String> errors) {

    public static ApiError of(HttpStatus status, String message, List<String> errors) {
        return new ApiError(status.value(), message, errors);
    }
}
