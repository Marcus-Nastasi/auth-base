package com.auth.auth.adapters.inbound.rest;

import com.auth.auth.adapters.inbound.input.AuthRequestDto;
import com.auth.auth.adapters.inbound.output.AuthResponseDto;
import com.auth.core.domain.AuthLogin;
import com.auth.core.ports.inbound.auth.AuthUseCasePort;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/iam")
public class IamController {

    private final AuthUseCasePort authUseCasePort;

    public IamController(final AuthUseCasePort authUseCasePort) {
        this.authUseCasePort = authUseCasePort;
    }

    @PostMapping
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid final AuthRequestDto dto) {
        final AuthLogin authLogin = authUseCasePort.login(dto.cpf(), dto.password());

        return ResponseEntity.ok(new AuthResponseDto(authLogin.getToken(), authLogin.getRefresh(), authLogin.getScope()));
    }
}
