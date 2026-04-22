package com.auth.auth.adapters.inbound.rest;

import com.auth.auth.adapters.inbound.output.KeysDto;
import com.auth.core.ports.inbound.auth.TokenPort;
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

    private final TokenPort tokenPort;

    public JwksController(final TokenPort tokenPort) {
        this.tokenPort = tokenPort;
    }

    @GetMapping("/jwks.json")
    public ResponseEntity<List<KeysDto>> keys() {
        final RSAPublicKey pub = RSAPublicKey.class.cast(tokenPort.getPublicKey());
        final String n = Base64.getUrlEncoder().withoutPadding().encodeToString(pub.getModulus().toByteArray());
        final String e = Base64.getUrlEncoder().withoutPadding().encodeToString(pub.getPublicExponent().toByteArray());

        final KeysDto keysDto = new KeysDto(new KeysDto.JwksDto(
                "RSA",
                tokenPort.getKid(),
                "sig",
                "RS256",
                n,
                e
        ));

        return ResponseEntity.ok(List.of(keysDto));
    }
}
