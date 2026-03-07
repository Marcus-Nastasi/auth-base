package com.auth.user.adapters.inbound.handler;

import com.auth.core.exceptions.NotFoundException;
import com.auth.user.adapters.inbound.exceptions.UnprocessableEntityException;
import com.auth.user.adapters.inbound.rest.UserController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = UserController.class)
public class UserExceptionHandler {

    @ExceptionHandler(value = UnprocessableEntityException.class)
    public ResponseEntity<Object> handleDomainException(UnprocessableEntityException e) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(value = NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException e) {
        return ResponseEntity.notFound().build();
    }
}
