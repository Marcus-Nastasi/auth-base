package com.auth.user.adapters.inbound.handler;

import com.auth.core.exceptions.NotFoundException;
import com.auth.core.shared.AppError;
import com.auth.user.adapters.inbound.exceptions.UnprocessableEntityException;
import com.auth.user.adapters.inbound.output.SuperSetErrorResponseDto;
import com.auth.user.adapters.inbound.rest.UserController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestControllerAdvice(assignableTypes = UserController.class)
public class UserExceptionHandler {

    @ExceptionHandler(value = UnprocessableEntityException.class)
    public ResponseEntity<Object> handleDomainException(final UnprocessableEntityException e) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(value = NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(final NotFoundException e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<Object> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        final String field = e.getFieldError() != null
                ? e.getFieldError().getField()
                : "";

        final String message = Arrays.stream(e.getMessage().split(";")).toList()
                .getLast()
                .replaceAll("\\[", "")
                .replaceAll("]", "")
                .replace("default message", "")
                .trim();

        final List<String> messageWordList = Arrays.stream(e.getMessage().split(" ")).toList();

        String attempted = null;
        for (int i = 0; i < messageWordList.size(); i++) {
            final String s = messageWordList.get(i);

            if ("rejected".equals(s)) {
                if ("value".equals(messageWordList.get(i + 1))) {
                    final String value = messageWordList.get(i + 2);

                    if (value != null && value.contains(";")) {
                        attempted = value.replaceAll(";", "");
                    } else {
                        attempted = value;
                    }
                }
            }
        }

        final var response = new SuperSetErrorResponseDto<>(List.of(AppError.builder()
                .message(message)
                .field(field)
                .attempted(attempted != null ? attempted : "")
                .build()));

        return ResponseEntity.badRequest().body(response);
    }
}
