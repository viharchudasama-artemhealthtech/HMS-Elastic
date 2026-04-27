package com.hms.user.mapper;

import com.hms.user.dto.UserResponseDTO;
import com.hms.user.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-27T10:59:19+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponseDTO toResponseDTO(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponseDTO.UserResponseDTOBuilder userResponseDTO = UserResponseDTO.builder();

        userResponseDTO.id( user.getId() );
        userResponseDTO.username( user.getUsername() );
        userResponseDTO.email( user.getEmail() );
        userResponseDTO.role( user.getRole() );
        userResponseDTO.enabled( user.getEnabled() );

        return userResponseDTO.build();
    }
}
