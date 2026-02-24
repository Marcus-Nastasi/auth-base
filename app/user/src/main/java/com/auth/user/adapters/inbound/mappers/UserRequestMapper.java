package com.auth.user.adapters.inbound.mappers;

import com.auth.core.domain.User;
import com.auth.user.adapters.inbound.input.UserRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserRequestMapper {

    UserRequestMapper INSTANCE = Mappers.getMapper(UserRequestMapper.class);

    @Mapping(source = "email", target = "email")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "first_name", target = "firstName")
    @Mapping(source = "last_name", target = "lastName")
    @Mapping(source = "cpf", target = "cpf")
    @Mapping(source = "birth_date", target = "birthDate")
    @Mapping(source = "user_role", target = "userRole")
    User toDomain(UserRequestDto userRequestDto);
}
