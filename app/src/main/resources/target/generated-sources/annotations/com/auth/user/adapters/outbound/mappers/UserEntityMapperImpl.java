package com.auth.user.adapters.outbound.mappers;

import com.auth.core.domain.User;
import com.auth.user.adapters.outbound.entity.UserEntity;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-14T01:21:48-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22.0.2 (Oracle Corporation)"
)
public class UserEntityMapperImpl implements UserEntityMapper {

    @Override
    public User toDomain(UserEntity userEntity) {
        if ( userEntity == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( userEntity.getId() );
        user.email( userEntity.getEmail() );
        user.cpf( userEntity.getCpf() );
        user.password( userEntity.getPassword() );
        user.firstName( userEntity.getFirstName() );
        user.lastName( userEntity.getLastName() );
        user.birthDate( userEntity.getBirthDate() );
        user.userRole( userEntity.getUserRole() );
        user.status( userEntity.getStatus() );
        user.createdAt( userEntity.getCreatedAt() );
        user.updatedAt( userEntity.getUpdatedAt() );
        user.inactivatedAt( userEntity.getInactivatedAt() );

        return user.build();
    }

    @Override
    public UserEntity toEntity(User user) {
        if ( user == null ) {
            return null;
        }

        UserEntity.UserEntityBuilder userEntity = UserEntity.builder();

        userEntity.id( user.getId() );
        userEntity.email( user.getEmail() );
        userEntity.cpf( user.getCpf() );
        userEntity.password( user.getPassword() );
        userEntity.firstName( user.getFirstName() );
        userEntity.lastName( user.getLastName() );
        userEntity.birthDate( user.getBirthDate() );
        userEntity.status( user.getStatus() );
        userEntity.userRole( user.getUserRole() );
        userEntity.createdAt( user.getCreatedAt() );
        userEntity.updatedAt( user.getUpdatedAt() );
        userEntity.inactivatedAt( user.getInactivatedAt() );

        return userEntity.build();
    }
}
