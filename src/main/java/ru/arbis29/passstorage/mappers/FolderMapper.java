package ru.arbis29.passstorage.mappers;

import org.mapstruct.Mapper;
import ru.arbis29.passstorage.domain.PasswordFolder;
import ru.arbis29.passstorage.model.FolderDTO;

@Mapper
public interface FolderMapper {
    FolderDTO fldToDTO(PasswordFolder passwordFolder);
    PasswordFolder DTOToFld(FolderDTO folderDTO);
}
