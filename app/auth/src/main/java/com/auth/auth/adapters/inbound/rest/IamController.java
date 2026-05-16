package com.auth.auth.adapters.inbound.rest;

import com.auth.auth.adapters.inbound.input.AuthRequestDto;
import com.auth.auth.adapters.inbound.output.AuthResponseDto;
import com.auth.core.ports.inbound.auth.AuthUseCasePort;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
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

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid final AuthRequestDto dto) {
        final var login = authUseCasePort.login(dto.cpf(), dto.password());

        final var resp = AuthResponseDto.builder()
                .accessToken(login.getToken())
                .refreshToken(login.getRefresh())
                .scopes(login.getScope())
                .build();

        return ResponseEntity.ok(resp);
    }
}
