package ru.arbis29.passstorage.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.model.FolderDTO;

import java.security.Principal;

public interface FolderService {
    Flux<FolderDTO> listFolders(Principal principal);
    Flux<FolderDTO> listSharedFolders(Principal principal);
    Mono<FolderDTO> getFolderById(String id, Principal principal);
    Mono<FolderDTO> saveNewFolder(FolderDTO folderDTO, Principal principal);

    Mono<Void> deleteFolderById(String id, Principal principal);
}
