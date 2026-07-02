package com.auth.user.adapters.inbound.mappers;

import com.auth.core.domain.User;
import com.auth.user.adapters.inbound.input.UserRequestDto;
import com.auth.user.adapters.inbound.input.UserUpdateRequestDto;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-02T02:44:27-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22.0.2 (Oracle Corporation)"
)
public class UserRequestMapperImpl implements UserRequestMapper {

    @Override
    public User toDomain(UserRequestDto userRequestDto) {
        if ( userRequestDto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.email( userRequestDto.email() );
        user.password( userRequestDto.password() );
        user.firstName( userRequestDto.firstName() );
        user.lastName( userRequestDto.lastName() );
        user.cpf( userRequestDto.cpf() );
        user.birthDate( userRequestDto.birthDate() );
        user.userRole( userRequestDto.userRole() );

        return user.build();
    }

    @Override
    public User toDomain(UserUpdateRequestDto userUpdateRequestDto) {
        if ( userUpdateRequestDto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.email( userUpdateRequestDto.email() );
        user.password( userUpdateRequestDto.password() );
        user.firstName( userUpdateRequestDto.firstName() );
        user.lastName( userUpdateRequestDto.lastName() );
        user.cpf( userUpdateRequestDto.cpf() );
        user.birthDate( userUpdateRequestDto.birthDate() );
        user.userRole( userUpdateRequestDto.userRole() );
        user.status( userUpdateRequestDto.status() );

        return user.build();
    }
}
