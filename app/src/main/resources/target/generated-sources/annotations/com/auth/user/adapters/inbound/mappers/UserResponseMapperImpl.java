package com.auth.user.adapters.inbound.mappers;

import com.auth.core.domain.User;
import com.auth.user.adapters.inbound.output.UserByIdResponseDto;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-14T01:21:48-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22.0.2 (Oracle Corporation)"
)
public class UserResponseMapperImpl implements UserResponseMapper {

    @Override
    public UserByIdResponseDto toUserByIdResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserByIdResponseDto.UserByIdResponseDtoBuilder userByIdResponseDto = UserByIdResponseDto.builder();

        userByIdResponseDto.role( user.getUserRole() );
        userByIdResponseDto.id( user.getId() );
        userByIdResponseDto.email( user.getEmail() );
        userByIdResponseDto.cpf( user.getCpf() );
        userByIdResponseDto.firstName( user.getFirstName() );
        userByIdResponseDto.lastName( user.getLastName() );
        userByIdResponseDto.birthDate( user.getBirthDate() );
        userByIdResponseDto.status( user.getStatus() );
        userByIdResponseDto.createdAt( user.getCreatedAt() );
        userByIdResponseDto.updatedAt( user.getUpdatedAt() );
        userByIdResponseDto.inactivatedAt( user.getInactivatedAt() );

        return userByIdResponseDto.build();
    }
}
