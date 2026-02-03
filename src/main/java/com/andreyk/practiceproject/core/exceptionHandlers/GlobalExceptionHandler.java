package com.andreyk.practiceproject.core.exceptionHandlers;

import com.andreyk.practiceproject.api.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.Unauthorized.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(HttpClientErrorException.Unauthorized ex) {
        return ResponseEntity.status(401)
                .body(new ErrorResponse(401, ex.getLocalizedMessage()));
    }

    @ExceptionHandler(HttpClientErrorException.Forbidden.class)
    public ResponseEntity<ErrorResponse> handleForbidden(HttpClientErrorException.Forbidden ex) {
        return ResponseEntity.status(403)
                .body(new ErrorResponse(403, ex.getLocalizedMessage()));
    }

    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<ErrorResponse> handleS3Exception(S3Exception ex) {
        return ResponseEntity.status(500)
                .body(new ErrorResponse(500, ex.getLocalizedMessage()));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientError(HttpClientErrorException ex) {
        return ResponseEntity.status(ex.getStatusCode().value())
                .body(new ErrorResponse(ex.getStatusCode().value(), ex.getLocalizedMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        return ResponseEntity.status(500)
                .body(new ErrorResponse(500, ex.getLocalizedMessage()));
    }
}
