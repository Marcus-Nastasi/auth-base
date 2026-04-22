package com.auth.user.adapters.inbound.rest;

import com.auth.core.domain.PageResponse;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

@Validated
@RestController
@RequestMapping(value = "/api/v1/user")
public class UserController {

    private final UserUseCasePort useCase;

    private final TokenPort tokenPort;

    private final HttpInterceptor idEqualsOrAdminInterceptor;

    @Autowired
    public UserController(final UserUseCasePort useCase,
                          final TokenPort tokenPort,
                          @Qualifier("IdEqualsOrAdminInterceptor")
                          final HttpInterceptor idEqualsOrAdminInterceptor) {
        this.useCase = useCase;
        this.tokenPort = tokenPort;
        this.idEqualsOrAdminInterceptor = idEqualsOrAdminInterceptor;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAllAuthorities('SCOPE_users.read','SCOPE_users.admin')")
    public ResponseEntity<PageResponse<UserByIdResponseDto>> getAll(@RequestParam(value = "page") final int page,
                                                                    @RequestParam(value = "size") final int size,
                                                                    HttpServletRequest httpServletRequest) {
        final PageResponse<User> result = useCase.findAll(page, size);

        final PageResponse<UserByIdResponseDto> response = PageResponse.<UserByIdResponseDto>builder()
                .page(result.page())
                .size(result.size())
                .nextPage(result.nextPage())
                .nextPageLink(httpServletRequest.getRequestURL().toString() +"?"+ httpServletRequest.getQueryString())
                .data(result.data().stream()
                        .map(UserResponseMapper.INSTANCE::toUserByIdResponse)
                        .collect(Collectors.toSet()))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<SuperSetResponseDto<UserByIdResponseDto>> getById(@PathVariable("id") UUID userId,
                                                                            @RequestHeader(value = "Authorization") String token) {
        idEqualsOrAdminInterceptor.validate(new Object[]{userId, token});

        final User user = useCase.findById(userId);

        return ResponseEntity.ok(new SuperSetResponseDto<>(UserResponseMapper.INSTANCE.toUserByIdResponse(user)));
    }

    @PostMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<SuperSetResponseDto<UserByIdResponseDto>> registre(@RequestBody @Valid UserRequestDto dto) {
        final User user = useCase.save(UserRequestMapper.INSTANCE.toDomain(dto));
        final var response = new SuperSetResponseDto<>(UserResponseMapper.INSTANCE.toUserByIdResponse(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = "/activate", produces = TEXT_HTML_VALUE)
    public ResponseEntity<String> activate(@RequestParam("token") String token) {
        try {
            final DecodedJWT d = (DecodedJWT) tokenPort.validate(token);
            final String email = d.getClaim("email").asString();
            final UUID userId = UUID.fromString(d.getSubject());

            final User user = useCase.activate(email, userId);

            final var responseMessage = String.format("""
                <h2>Seu e-mail %s está ativo, feche a aba e faça o login.</h2>
            """, user.getEmail());

            return ResponseEntity.accepted().body(responseMessage);
        } catch (Exception e) {
            throw new ForbiddenException(e.getMessage(), e);
        }
    }

    @GetMapping(value = "/resend", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> resendEmail(@RequestParam("email") String email) {
        try {
            useCase.resendEmail(email);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(value = "/inactivate", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<SuperSetResponseDto<UserByIdResponseDto>> inactivate(@RequestParam("email") String email,
                                                                               @RequestHeader(value = "Authorization") String token) {
        final User fromEmail = useCase.findByEmail(email);

        idEqualsOrAdminInterceptor.validate(new Object[]{fromEmail.getId(), token});

        final User user = useCase.inactivate(email);

        final var response = new SuperSetResponseDto<>(UserResponseMapper.INSTANCE.toUserByIdResponse(user));

        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<SuperSetResponseDto<UserByIdResponseDto>> update(@PathVariable("id") UUID id,
                                                                           @RequestBody @Valid UserUpdateRequestDto userUpdateRequestDto,
                                                                           @RequestHeader(value = "Authorization") String token) {
        idEqualsOrAdminInterceptor.validate(new Object[]{id, token});

        final User userReceived = UserRequestMapper.INSTANCE.toDomain(userUpdateRequestDto);
        userReceived.setId(id);

        final User updated = useCase.save(userReceived);

        final var response = new SuperSetResponseDto<UserByIdResponseDto>(
                UserResponseMapper.INSTANCE.toUserByIdResponse(updated)
        );

        return ResponseEntity.accepted().body(response);
    }
}
