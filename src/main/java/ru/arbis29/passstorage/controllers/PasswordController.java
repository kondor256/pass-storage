package ru.arbis29.passstorage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.model.PasswordDTO;
import ru.arbis29.passstorage.model.SharePassRequestDTO;
import ru.arbis29.passstorage.model.UserDTO;
import ru.arbis29.passstorage.services.PasswordService;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PasswordController {
    private static final String BASE_URI = "/api/v1/pass";

    private final PasswordService passwordService;

    @GetMapping(BASE_URI + "/test")
    Mono<String> test(){
        return Mono.just("test");
    }

    @GetMapping(BASE_URI + "/empty")
    Mono<PasswordDTO> getEmptyPass(Principal principal){
        return Mono.just(PasswordDTO.builder().build());
    }
    @GetMapping(BASE_URI + "/list")
    Flux<PasswordDTO> getPasswordList(Principal principal){
        return passwordService.listPasswords(principal);
    }

    @PostMapping(BASE_URI)
    Mono<ResponseEntity<PasswordDTO>> createNewPass(Principal principal, @Validated @RequestBody PasswordDTO passwordDTO){
        return passwordService.saveNewPass(passwordDTO,principal)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping(BASE_URI)
    Mono<ResponseEntity<Void>> deletePass(Principal principal, @Validated @RequestBody PasswordDTO passwordDTO){
//        return passwordService.getPassById(passwordDTO.getId())
//                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
//                .map(foundPass -> passwordService.deletePassById(foundPass.getId()))
//                .thenReturn(ResponseEntity.ok().build());
        return passwordService.deletePassById(passwordDTO.getId(),principal)
                .thenReturn(ResponseEntity.ok().build());
    }

    @PostMapping(BASE_URI+"/share")
    Mono<ResponseEntity<Void>> sharePass(Principal principal, @Validated @RequestBody SharePassRequestDTO request){
//        return passwordService.sharePass(passwordDTO.getId(),passwordDTO.getOwnerUserId(),principal)
        return passwordService.sharePass(request.getPassId(),request.getUserIdList(),principal)
                .thenReturn(ResponseEntity.ok().build());
    }

    @GetMapping(BASE_URI+"/shared_users/{pass_id}")
    Flux<UserDTO> getSharedPassUsers(Principal principal, @PathVariable(name = "pass_id") String passId){
        return passwordService.getSharedPassUsers(passId, principal);
    }
}
