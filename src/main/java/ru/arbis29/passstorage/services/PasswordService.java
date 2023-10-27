package ru.arbis29.passstorage.services;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.model.PasswordDTO;

@Service
public interface PasswordService {
    Flux<PasswordDTO> listPasswords();
    Mono<PasswordDTO> getPassById(String id);
    Mono<PasswordDTO> saveNewPass(PasswordDTO passwordDTO);

}
