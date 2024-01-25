package ru.arbis29.passstorage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.model.PasswordDTO;
import ru.arbis29.passstorage.services.PasswordService;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@Slf4j
public class passwordController {
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
        log.warn(principal.getName());
        return passwordService.listPasswords();
    }

    @PostMapping(BASE_URI)
    Mono<ResponseEntity<Void>> createNewPass(@Validated @RequestBody PasswordDTO passwordDTO){
        return passwordService.saveNewPass(passwordDTO)
                .map(savedPass -> ResponseEntity.ok().build());
    }
}
