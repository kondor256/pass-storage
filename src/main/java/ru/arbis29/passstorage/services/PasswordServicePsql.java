package ru.arbis29.passstorage.services;

import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.domain.AppUser;
import ru.arbis29.passstorage.domain.StoredPassword;
import ru.arbis29.passstorage.mappers.PasswordMapper;
import ru.arbis29.passstorage.model.PasswordDTO;
import ru.arbis29.passstorage.repo.AppUserRepo;
import ru.arbis29.passstorage.repo.PasswordRepo;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordServicePsql implements PasswordService {

    private final AppUserRepo appUserRepo;
    private final PasswordRepo passwordRepo;
    private final PasswordMapper passwordMapper;
    private final UserService userService;
    private final KeysService keysService;

    private Mono<List<String>> getUserIdList(String userName){
        return appUserRepo.findAllByLogin(userName)
                .collectList()
                .flatMap(listUsers -> Mono.just(listUsers.stream().map(AppUser::getId).toList()));
    }

    private Mono<StoredPassword> decryptPass(StoredPassword pass, String userId){
        return keysService.decryptPass(userId, pass.getEncryptedPassword(),null)
                .map(decryptedPass -> {
                    pass.setEncryptedPassword(null);
                    pass.setPassword(decryptedPass);
                    return pass;
                });
    }
    @Override
    public Flux<PasswordDTO> listPasswords(Principal principal) {
        final String userId = userService.getUserId(principal);
        return passwordRepo.findAllByOwnerUserId(userId)
//                .map(pass -> {
//                    log.warn(String.valueOf(pass));
//                    return passwordMapper.passToPassDTO(pass);
//                });
//                .map(passwordMapper::passToPassDTO);
                .flatMap(storedPassword -> {
                    if (StringUtil.isNullOrEmpty(storedPassword.getPassword()) && storedPassword.getEncryptedPassword() != null) {
//                        return keysService.decryptPass(userId, storedPassword.getEncryptedPassword(),null)
//                               .map(pass -> {
//                                   storedPassword.setPassword(pass);
//                                   storedPassword.setEncryptedPassword(null);
//                                   return passwordMapper.passToPassDTO(storedPassword);
//                               });
                        return decryptPass(storedPassword, userId)
                                .map(passwordMapper::passToPassDTO);
                    }
                    return Mono.just(passwordMapper.passToPassDTO(storedPassword));
                });
    }

    @Override
    public Mono<PasswordDTO> getPassById(String id, Principal principal) {
        final String userId = userService.getUserId(principal);
        return passwordRepo.findById(id)
                .flatMap(storedPassword -> {
                    if (storedPassword.getOwnerUserId().equals(userId)) {
                        if (StringUtil.isNullOrEmpty(storedPassword.getPassword()) && storedPassword.getEncryptedPassword() != null) {
                            return decryptPass(storedPassword, userId)
                                    .map(passwordMapper::passToPassDTO);
                        }
                        return Mono.just(passwordMapper.passToPassDTO(storedPassword));
                    }
                    else return Mono.empty();
                });
                //.map(passwordMapper::passToPassDTO);
//                .flatMap(storedPassword -> appUserRepo.findAllByLogin(userName)
//                        .collectList()
//                        .flatMap(listUsers -> listUsers.stream().map(AppUser::getId).toList().contains(storedPassword.getOwnerUserId())?
//                                Mono.justOrEmpty(passwordMapper.passToPassDTO(storedPassword)):
//                                Mono.empty()));
    }

    private Mono<StoredPassword> encryptPass(StoredPassword pass, String userId){
        return keysService.encryptPass(userId, pass.getPassword())
                .map(encryptedPass -> {
                    pass.setEncryptedPassword(encryptedPass);
                    pass.setPassword(null);
                    return pass;
                });
    }
    @Override
    public Mono<PasswordDTO> saveNewPass(PasswordDTO passwordDTO, Principal principal) {
        final var pass = passwordMapper.passDTOToPass(passwordDTO);
        final String userId = userService.getUserId(principal);

        if (StringUtil.isNullOrEmpty(passwordDTO.getId())) {
            return appUserRepo.findById(userId)
                    .flatMap(user -> {
                        pass.setOwnerUserId(user.getId());
                        return encryptPass(pass, userId)
                                .flatMap(passWithEncrypt -> passwordRepo.save(passWithEncrypt)
                                    .map(passwordMapper::passToPassDTO)
                                );
                    });
        }

        return passwordRepo.findById(passwordDTO.getId())
                .flatMap(storedPassword -> storedPassword.getOwnerUserId().equals(userId)?
                        encryptPass(pass,userId)
                                .flatMap(passWithEncrypt -> passwordRepo.save(passWithEncrypt)
                                        .map(passwordMapper::passToPassDTO)):
                        Mono.empty());
    }

    @Override
    public Mono<Void> deletePassById(String id, Principal principal) {
        final String userId = userService.getUserId(principal);
        return passwordRepo.findById(id).flatMap(
                storedPassword -> storedPassword.getOwnerUserId().equals(userId)?
                        passwordRepo.deleteById(id):
                        Mono.empty()
//                        storedPassword -> getUserIdList(userName).flatMap(
//                                userIdList -> userIdList.contains(storedPassword.getOwnerUserId())
//                                ?passwordRepo.deleteById(id)
//                                :Mono.empty()
//                        )
                );
    }
}
