package com.auth.user.adapters.inbound.mappers;

import com.auth.core.domain.User;
import com.auth.user.adapters.inbound.input.UserRequestDto;
import com.auth.user.adapters.inbound.input.UserUpdateRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserRequestMapper {

    UserRequestMapper INSTANCE = Mappers.getMapper(UserRequestMapper.class);

    @Mapping(source = "email", target = "email")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "cpf", target = "cpf")
    @Mapping(source = "birthDate", target = "birthDate")
    @Mapping(source = "userRole", target = "userRole")
    User toDomain(UserRequestDto userRequestDto);

    @Mapping(source = "email", target = "email")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "cpf", target = "cpf")
    @Mapping(source = "birthDate", target = "birthDate")
    @Mapping(source = "userRole", target = "userRole")
    @Mapping(source = "status", target = "status")
    User toDomain(UserUpdateRequestDto userUpdateRequestDto);
}
