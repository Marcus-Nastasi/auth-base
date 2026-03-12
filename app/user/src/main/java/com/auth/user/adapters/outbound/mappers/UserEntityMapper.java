package com.auth.user.adapters.outbound.mappers;

import com.auth.core.domain.User;
import com.auth.user.adapters.outbound.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserEntityMapper {

    UserEntityMapper INSTANCE = Mappers.getMapper(UserEntityMapper.class);

    User toDomain(UserEntity userEntity);

    UserEntity toEntity(User user);
}
