package ru.arbis29.passstorage.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.model.PasswordDTO;

import java.security.Principal;

public interface PasswordService {
    Flux<PasswordDTO> listPasswords(Principal principal);
    Mono<PasswordDTO> getPassById(String id,Principal principal);
    Mono<PasswordDTO> saveNewPass(PasswordDTO passwordDTO,Principal principal);
    Mono<Void> deletePassById(String id,Principal principal);
}
