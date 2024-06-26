package ru.arbis29.passstorage.services;

import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.domain.PasswordFolder;
import ru.arbis29.passstorage.domain.SharedPassword;
import ru.arbis29.passstorage.domain.StoredPassword;
import ru.arbis29.passstorage.mappers.FolderMapper;
import ru.arbis29.passstorage.model.FolderDTO;
import ru.arbis29.passstorage.repo.AppUserRepo;
import ru.arbis29.passstorage.repo.FolderRepo;
import ru.arbis29.passstorage.repo.PasswordRepo;
import ru.arbis29.passstorage.repo.SharedPasswordRepo;

import java.security.Principal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class FolderServicePsql implements FolderService {
    private final AppUserRepo userRepo;
    private final FolderRepo folderRepo;
    private final PasswordRepo passwordRepo;
    private final SharedPasswordRepo sharedPasswordRepo;
    private final FolderMapper folderMapper;
    private final UserService userService;
    @Override
    public Flux<FolderDTO> listFolders(Principal principal) {
        final String userId = userService.getUserId(principal);
        return folderRepo.findAllByOwnerUserIdOrderByName(userId)
                .map(folderMapper::fldToDTO);
    }

    @Override
    public Flux<FolderDTO> listSharedFolders(Principal principal) {
        final String userId = userService.getUserId(principal);
        Flux<PasswordFolder> folders = sharedPasswordRepo.findAllByUserId(userId)
                .map(SharedPassword::getPasswordId)
                .flatMap(passwordRepo::findById)
                .filter(storedPassword -> storedPassword.getFolderId() != null)
                .map(StoredPassword::getFolderId)
                .collectList().flux()
                .flatMap(folderRepo::findAllById);
        Flux<FolderDTO> userFolders = sharedPasswordRepo.findAllByUserId(userId)
                .map(SharedPassword::getPasswordId)
                .flatMap(passwordRepo::findById)
                .map(StoredPassword::getOwnerUserId)
                .collectList().flux()
                .flatMap(userRepo::findAllById)
                .map(appUser -> FolderDTO.builder()
                        .id(appUser.getId())
                        .name(appUser.getName()+" ("+appUser.getLogin()+")")
                        .ownerUserId(userId)
                        .shared(true)
                        .build());
        return folders
                .map(folderMapper::fldToDTO)
                .map(folderDTO -> {
                    folderDTO.setFolderId(folderDTO.getOwnerUserId());
                    folderDTO.setShared(true);
                    return folderDTO;
                })
                .concatWith(userFolders);
    }

    @Override
    public Mono<FolderDTO> getFolderById(String id, Principal principal) {
        final String userId = userService.getUserId(principal);
        return folderRepo.findFirstByIdAndOwnerUserId(id,userId)
                .map(folderMapper::fldToDTO);
    }

    @Override
    public Mono<FolderDTO> saveNewFolder(FolderDTO folderDTO, Principal principal)
    {
        final PasswordFolder folder = folderMapper.DTOToFld(folderDTO);
        final String userId = userService.getUserId(principal);
        folder.setOwnerUserId(userId);
        if (StringUtil.isNullOrEmpty(folder.getId())) {
            return folderRepo.save(folder)
                    .map(folderMapper::fldToDTO);
        }

//        return folderRepo.save(folderMapper.DTOToFld(folderDTO))
//                .map(folderMapper::fldToDTO);
        return folderRepo.findFirstByIdAndOwnerUserId(folder.getId(), userId)
                .flatMap(foundFolder -> folderRepo.save(folder)
                        .map(folderMapper::fldToDTO));
    }

    @Override
    public Mono<Void> deleteFolderById(String id, Principal principal) {
        final String userId = userService.getUserId(principal);
        return folderRepo.deleteByIdAndOwnerUserId(id, userId);
    }
}
