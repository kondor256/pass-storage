package ru.arbis29.passstorage.mappers;

import org.mapstruct.Mapper;
import ru.arbis29.passstorage.domain.AppUser;
import ru.arbis29.passstorage.model.UserDTO;

@Mapper
public interface UserMapper {
    AppUser DTOtoUser(UserDTO userDTO);
    UserDTO userToDTO(AppUser user);
}
