package ru.arbis29.passstorage.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.model.PasswordDTO;

public interface PasswordService {
    Flux<PasswordDTO> listPasswords();
    Mono<PasswordDTO> getPassById(String id);
    Mono<PasswordDTO> saveNewPass(PasswordDTO passwordDTO);

}
