package com.auth.user.adapters.inbound.handler;

import com.auth.core.exceptions.NotFoundException;
import com.auth.user.adapters.inbound.exceptions.UnprocessableEntityException;
import com.auth.user.adapters.inbound.rest.UserController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
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

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<Object> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        final String field = e.getFieldError() != null
                ? e.getFieldError().getField()
                : "";

        final String message = Arrays.stream(e.getMessage().split(";"))
                .toList()
                .getLast()
                .replaceAll("\\[", "")
                .replaceAll("]", "")
                .replace("default message", "")
                .trim();

        return ResponseEntity.badRequest().body(Map.of("field", field, "message", message));
    }
}
