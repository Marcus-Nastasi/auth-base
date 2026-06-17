package com.auth.auth.adapters.inbound.handler;

import com.auth.core.exceptions.ForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice()
public class AuthExceptionHandler {

    @ExceptionHandler(value = ForbiddenException.class)
    public ResponseEntity<Object> handleForbiddenException(ForbiddenException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
