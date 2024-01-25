package ru.arbis29.passstorage.mappers;

import org.mapstruct.Mapper;
import ru.arbis29.passstorage.domain.StoredPassword;
import ru.arbis29.passstorage.model.PasswordDTO;

@Mapper
public interface PasswordMapper {
    PasswordDTO passToPassDTO(StoredPassword password);
    StoredPassword passDTOToPass(PasswordDTO passwordDTO);
}
