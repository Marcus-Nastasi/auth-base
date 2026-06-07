package com.auth.auth.adapters.inbound.mappers;

import com.auth.auth.adapters.inbound.output.ClientRegistrationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.UUID;

@Mapper
public interface ClientRegistrationDtoMapper {

   ClientRegistrationDtoMapper INSTANCE = Mappers.getMapper(ClientRegistrationDtoMapper.class);

   @Mapping(target = "clientId", source = "client.clientId")
   @Mapping(target = "clientSecret", source = "clientSecret")
   @Mapping(target = "grantTypes", source = "client.authorizationGrantTypes")
   @Mapping(target = "scopes", source = "client.scopes")
   ClientRegistrationResponse response(final RegisteredClient client, final UUID clientSecret);
}
