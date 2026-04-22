package com.auth.auth.adapters.inbound.rest;

import com.auth.auth.adapters.inbound.input.AuthRequestDto;
import com.auth.auth.adapters.inbound.output.AuthResponseDto;
import com.auth.core.domain.enums.UserRole;
import com.auth.core.ports.inbound.auth.AuthUseCasePort;
import com.auth.core.ports.inbound.auth.TokenPort;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/iam")
public class IamController {

    private final AuthUseCasePort authUseCasePort;

    private final TokenPort tokenPort;

    public IamController(final AuthUseCasePort authUseCasePort, final TokenPort tokenPort) {
        this.authUseCasePort = authUseCasePort;
        this.tokenPort = tokenPort;
    }

    @PostMapping
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid final AuthRequestDto dto) {
        final String token = authUseCasePort.login(dto.cpf(), dto.password());
        final DecodedJWT d = (DecodedJWT) tokenPort.validate(token);
        final String scopes = d.getClaim("scope").as(String.class);

        return ResponseEntity.ok(new AuthResponseDto(token, null, scopes));
    }
}
