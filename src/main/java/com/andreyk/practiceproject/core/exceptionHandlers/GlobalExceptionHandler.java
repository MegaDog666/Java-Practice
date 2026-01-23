package com.andreyk.practiceproject.core.exceptionHandlers;

import com.andreyk.practiceproject.api.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.Unauthorized.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(HttpClientErrorException.Unauthorized ex) {
        return ResponseEntity.status(401)
                .body(new ErrorResponse(401, "Пользователь не авторизован"));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientError(HttpClientErrorException ex) {
        return ResponseEntity.status(ex.getStatusCode().value())
                .body(new ErrorResponse(ex.getStatusCode().value(), ex.getStatusText()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        return ResponseEntity.status(500)
                .body(new ErrorResponse(500, "internal server error"));
    }
}
