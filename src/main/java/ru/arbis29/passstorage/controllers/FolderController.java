package ru.arbis29.passstorage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.model.FolderDTO;
import ru.arbis29.passstorage.services.FolderService;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FolderController {
    public static final String BASE_URI = "/api/v1/folder";

    private final FolderService folderService;

    @GetMapping(BASE_URI + "/empty")
    Mono<FolderDTO> getEmptyFolder(Principal principal){
        return Mono.just(FolderDTO.builder().build());
    }
    @GetMapping(BASE_URI + "/list")
    Flux<FolderDTO> getFolderList(Principal principal){
        return folderService.listFolders(principal)
                .concatWith(folderService.listSharedFolders(principal));
    }

    @PostMapping(BASE_URI)
    Mono<ResponseEntity<FolderDTO>> saveFolder(Principal principal, @Validated @RequestBody FolderDTO folderDTO) {
        return folderService.saveNewFolder(folderDTO, principal)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping(BASE_URI)
    Mono<ResponseEntity<Void>> deleteFolder(Principal principal, @Validated @RequestBody FolderDTO folderDTO) {
//        return folderService.getFolderById(folderDTO.getId())
//                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
//                .map(foundFolder -> folderService.deleteFolderById(folderDTO.getId()))
//                .thenReturn(ResponseEntity.ok().build());
        return folderService.deleteFolderById(folderDTO.getId(), principal)
                .thenReturn(ResponseEntity.ok().build());
    }
}
