package com.auth.user.adapters.inbound.mappers;

import com.auth.core.domain.User;
import com.auth.user.adapters.inbound.output.UserByIdResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserResponseMapper {

    UserResponseMapper INSTANCE = Mappers.getMapper(UserResponseMapper.class);

    @Mapping(source = "userRole", target = "role")
    UserByIdResponseDto toUserByIdResponse(User user);
}
