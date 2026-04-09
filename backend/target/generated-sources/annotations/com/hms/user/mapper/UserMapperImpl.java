package com.hms.user.mapper;

import com.hms.user.dto.UserResponseDTO;
import com.hms.user.entity.User;
import org.springframework.stereotype.Component;

/*
@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-09T12:04:40+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
*/
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponseDTO toResponseDTO(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponseDTO.UserResponseDTOBuilder userResponseDTO = UserResponseDTO.builder();

        userResponseDTO.email( user.getEmail() );
        userResponseDTO.enabled( user.getEnabled() );
        userResponseDTO.id( user.getId() );
        userResponseDTO.role( user.getRole() );
        userResponseDTO.username( user.getUsername() );

        return userResponseDTO.build();
    }
}
