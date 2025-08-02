package ru.noleg.bankcards.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.noleg.bankcards.dto.user.ProfileDto;
import ru.noleg.bankcards.dto.user.UserDto;
import ru.noleg.bankcards.entity.User;
import ru.noleg.bankcards.security.dto.SignUp;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserDto mapToDto(User user);

    ProfileDto mapToProfileDto(User user);

    List<UserDto> mapToUserDtos(List<User> users);

    User mapToRegisterEntityFromSignUp(SignUp signUp);
}
