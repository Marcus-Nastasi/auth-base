package com.auth.auth.adapters.inbound.mappers;

import com.auth.auth.adapters.inbound.output.ClientRegistrationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

@Mapper
public interface ClientRegistrationDtoMapper {

   ClientRegistrationDtoMapper INSTANCE = Mappers.getMapper(ClientRegistrationDtoMapper.class);

   @Mapping(target = "appId", source = "client.id")
   @Mapping(target = "clientId", source = "client.clientId")
   @Mapping(target = "clientSecret", source = "clientSecret")
   @Mapping(target = "grantTypes", source = "client.authorizationGrantTypes", qualifiedByName = "getGrantTypes")
   @Mapping(target = "scopes", source = "client.scopes")
   @Mapping(target = "redirectUris", source = "client.redirectUris")
   @Mapping(target = "message", constant = "Save your client_secret in a safe place, you cannot consult it later")
   ClientRegistrationResponse response(final RegisteredClient client, final UUID clientSecret);

   @Named(value = "getGrantTypes")
   default Set<String> getGrantTypes(final Set<AuthorizationGrantType> authorizationGrantTypes) {
      if (isEmpty(authorizationGrantTypes)) return emptySet();
      return authorizationGrantTypes.stream()
           .filter(Objects::nonNull)
           .map(AuthorizationGrantType::getValue)
           .collect(toSet());
   }
}
