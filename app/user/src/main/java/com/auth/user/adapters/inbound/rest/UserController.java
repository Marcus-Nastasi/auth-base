package com.auth.user.adapters.inbound.rest;

import com.auth.core.domain.User;
import com.auth.core.exceptions.ForbiddenException;
import com.auth.core.ports.inbound.auth.HttpInterceptor;
import com.auth.core.ports.inbound.auth.TokenPort;
import com.auth.core.ports.inbound.user.UserUseCasePort;
import com.auth.user.adapters.inbound.input.UserRequestDto;
import com.auth.user.adapters.inbound.input.UserUpdateRequestDto;
import com.auth.user.adapters.inbound.mappers.UserRequestMapper;
import com.auth.user.adapters.inbound.mappers.UserResponseMapper;
import com.auth.user.adapters.inbound.output.SuperSetResponseDto;
import com.auth.user.adapters.inbound.output.UserByIdResponseDto;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/user")
public class UserController {

    private final UserUseCasePort useCase;

    private final TokenPort tokenPort;

    @Qualifier("IdEqualsOrAdminInterceptor")
    private final HttpInterceptor idEqualsOrAdminInterceptor;

    public UserController(final UserUseCasePort useCase, final TokenPort tokenPort, final HttpInterceptor idEqualsOrAdminInterceptor) {
        this.useCase = useCase;
        this.tokenPort = tokenPort;
        this.idEqualsOrAdminInterceptor = idEqualsOrAdminInterceptor;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getAll() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuperSetResponseDto<UserByIdResponseDto>> getById(@PathVariable("id") UUID userId,
                                                                            @RequestHeader(value = "Authorization") String token) {
//        idEqualsOrAdminInterceptor.validate(new Object[]{userId, token});

        final User user = useCase.findById(userId);

        return ResponseEntity.ok(new SuperSetResponseDto<>(UserResponseMapper.INSTANCE.toUserByIdResponse(user)));
    }

    @PostMapping
    public ResponseEntity<SuperSetResponseDto<UserByIdResponseDto>> registre(@RequestBody @Valid UserRequestDto dto) {
        final User user = useCase.save(UserRequestMapper.INSTANCE.toDomain(dto));
        final var response = new SuperSetResponseDto<>(UserResponseMapper.INSTANCE.toUserByIdResponse(user));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activate(@RequestParam("token") String token) {
        try {
            final DecodedJWT d = (DecodedJWT) tokenPort.validate(token);
            final String email = d.getClaim("email").asString();
            final UUID userId = UUID.fromString(d.getSubject());

            final User user = useCase.activate(email, userId);

            return ResponseEntity.accepted().body("""
                <h2>E-mail ativo, feche a aba e faça o login.</h2>
            """);
        } catch (Exception e) {
            throw new ForbiddenException(e.getMessage(), e);
        }
    }

    @GetMapping("/resend")
    public ResponseEntity<String> resendEmail(@RequestParam("email") String email) {
        try {
            useCase.resendEmail(email);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(value = "/inactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuperSetResponseDto<UserByIdResponseDto>> inactivate(@RequestParam("email") String email,
                                                                               @RequestHeader(value = "Authorization") String token) {
        final User fromEmail = useCase.findByEmail(email);

//        idEqualsOrAdminInterceptor.validate(new Object[]{fromEmail.getId(), token});

        final User user = useCase.inactivate(email);

        final var response = new SuperSetResponseDto<>(UserResponseMapper.INSTANCE.toUserByIdResponse(user));

        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuperSetResponseDto<UserByIdResponseDto>> update(@PathVariable("id") UUID id,
                                                                           @RequestBody @Valid UserUpdateRequestDto userUpdateRequestDto) {
        final User userReceived = UserRequestMapper.INSTANCE.toDomain(userUpdateRequestDto);
        userReceived.setId(id);

        final User updated = useCase.save(userReceived);

        final SuperSetResponseDto<UserByIdResponseDto> response = new SuperSetResponseDto<>(
                UserResponseMapper.INSTANCE.toUserByIdResponse(updated)
        );

        return ResponseEntity.accepted().body(response);
    }
}
