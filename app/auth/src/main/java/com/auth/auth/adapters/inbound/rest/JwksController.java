package com.auth.auth.adapters.inbound.rest;

import com.auth.auth.adapters.inbound.output.KeysDto;
import com.auth.auth.infra.impl.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/.well-known")
public final class JwksController {

    private final TokenService tokenService;

    public JwksController(final TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/jwks.json")
    public ResponseEntity<List<KeysDto>> keys() {
        final RSAPublicKey pub = tokenService.getPublicKey();
        final String n = Base64.getUrlEncoder().withoutPadding().encodeToString(pub.getModulus().toByteArray());
        final String e = Base64.getUrlEncoder().withoutPadding().encodeToString(pub.getPublicExponent().toByteArray());

        final KeysDto keysDto = new KeysDto(new KeysDto.JwksDto(
                "RSA",
                tokenService.getKid(),
                "sig",
                "RS256",
                n,
                e
        ));

        return ResponseEntity.ok(List.of(keysDto));
    }
}
