package ru.arbis29.passstorage.services;

import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.domain.StoredPassword;
import ru.arbis29.passstorage.model.PasswordDTO;
import ru.arbis29.passstorage.model.UserDTO;

import java.security.Principal;
import java.util.List;

public interface PasswordService {
    Flux<PasswordDTO> listPasswords(Principal principal);
    Mono<PasswordDTO> getPassById(String id,Principal principal);
    Mono<PasswordDTO> saveNewPass(PasswordDTO passwordDTO,Principal principal);
    Mono<Void> deletePassById(String id,Principal principal);
    Mono<Void> sharePass(String passId, List<String> userIdList, Principal principal);
    Flux<UserDTO> getSharedPassUsers(String passId, Principal principal);
}
