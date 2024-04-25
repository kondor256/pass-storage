package ru.arbis29.passstorage.services;

import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.ArrayUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.domain.AppUser;
import ru.arbis29.passstorage.domain.SharedPassword;
import ru.arbis29.passstorage.domain.StoredPassword;
import ru.arbis29.passstorage.mappers.PasswordMapper;
import ru.arbis29.passstorage.mappers.UserMapper;
import ru.arbis29.passstorage.model.PasswordDTO;
import ru.arbis29.passstorage.model.UserDTO;
import ru.arbis29.passstorage.repo.AppUserRepo;
import ru.arbis29.passstorage.repo.PasswordRepo;
import ru.arbis29.passstorage.repo.SharedPasswordRepo;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordServicePsql implements PasswordService {

    private final AppUserRepo appUserRepo;
    private final PasswordRepo passwordRepo;
    private final SharedPasswordRepo sharedPasswordRepo;
    private final PasswordMapper passwordMapper;
    private final UserMapper userMapper;
    private final UserService userService;
    private final KeysService keysService;

    private Mono<List<String>> getUserIdList(String userName){
        return appUserRepo.findAllByLogin(userName)
                .map(AppUser::getId)
                .collectList();
//                .flatMap(listUsers -> Mono.just(listUsers.stream().map(AppUser::getId).toList()));
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
        Flux<PasswordDTO> myPasswords = passwordRepo.findAllByOwnerUserId(userId)
                .flatMap(storedPassword -> {
                    if (StringUtil.isNullOrEmpty(storedPassword.getPassword()) && storedPassword.getEncryptedPassword() != null) {
                        return decryptPass(storedPassword, userId)
                                .map(passwordMapper::passToPassDTO)
                                .map(passwordDTO -> {
                                    passwordDTO.setShared(false);
                                    return passwordDTO;
                                });
                    }
                    PasswordDTO passwordDTO = passwordMapper.passToPassDTO(storedPassword);
                    passwordDTO.setShared(false);
                    return Mono.just(passwordDTO);
                });
        Flux<PasswordDTO> sharedPasswords = sharedPasswordRepo.findAllByUserId(userId)
                .flatMap(sharedPassword -> passwordRepo.findById(sharedPassword.getPasswordId())
                    .flatMap(storedPassword -> {
                        if (StringUtil.isNullOrEmpty(storedPassword.getPassword()) && storedPassword.getEncryptedPassword() != null) {
                            storedPassword.setEncryptedPassword(sharedPassword.getEncryptedPassword());
                            return decryptPass(storedPassword, userId)
                                    .map(passwordMapper::passToPassDTO)
                                    .map(passwordDTO -> {
                                        passwordDTO.setShared(true);
                                        if (passwordDTO.getFolderId() == null) passwordDTO.setFolderId(storedPassword.getOwnerUserId());
                                        return passwordDTO;
                                    });
                        }
                        PasswordDTO passwordDTO = passwordMapper.passToPassDTO(storedPassword);
                        passwordDTO.setShared(true);
                        if (passwordDTO.getFolderId() == null) passwordDTO.setFolderId(storedPassword.getOwnerUserId());
                        return Mono.just(passwordDTO);
                    }));

        return Flux.concat(myPasswords,sharedPasswords);
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

    @Transactional
    Mono<Void> deletePassByIdInTransaction(String id){
        return sharedPasswordRepo.deleteAllByPasswordId(id)
                .then(passwordRepo.deleteById(id));
    }
    @Override
    public Mono<Void> deletePassById(String id, Principal principal) {
        final String userId = userService.getUserId(principal);
        return passwordRepo.findById(id).flatMap(
                storedPassword -> storedPassword.getOwnerUserId().equals(userId)?
                        deletePassByIdInTransaction(id):
                        Mono.empty()
                );
    }

    @Transactional
    public Mono<Void> sharePassUserList(StoredPassword pass, List<String> userIdList, String currentUserId){
        return sharedPasswordRepo.deleteAllByPasswordId(pass.getId()).then(
            Flux.fromIterable(userIdList).flatMap(userId -> {
                if (pass.getOwnerUserId().equals(userId)) return Mono.empty();//Nothing to do

                if (pass.getEncryptedPassword() != null && pass.getEncryptedPassword().length > 0)
                    return keysService.decryptPass(currentUserId, pass.getEncryptedPassword(), null).flatMap(decryptedPass -> {
                        return keysService.encryptPass(userId, decryptedPass).flatMap(encriptedPass -> {
                            SharedPassword sharedPassword = SharedPassword.builder()
                                    .passwordId(pass.getId())
                                    .userId(userId)
                                    .encryptedPassword(encriptedPass)
                                    .build();
                            return sharedPasswordRepo.save(sharedPassword).flatMap(saved -> Mono.empty());
                        });
                    });

                SharedPassword sharedPassword = SharedPassword.builder()
                        .passwordId(pass.getId())
                        .userId(userId)
                        .build();
                return sharedPasswordRepo.save(sharedPassword).flatMap(saved -> Mono.empty());
            }).then());
    }
    @Override
    public Mono<Void> sharePass(String passId, List<String> userIdList, Principal principal) {
        final String currentUserId = userService.getUserId(principal);
        return passwordRepo.findById(passId).flatMap(foundPass -> {
            if (!foundPass.getOwnerUserId().equals(currentUserId)) return Mono.error(new Exception("It's not yours password!"));

            return sharePassUserList(foundPass,userIdList, currentUserId);
        });
    }

    @Override
    public Flux<UserDTO> getSharedPassUsers(String passId, Principal principal) {
        final String currentUserId = userService.getUserId(principal);
        return passwordRepo.findById(passId).flux().flatMap(foundPass -> {
            if (!foundPass.getOwnerUserId().equals(currentUserId)) return Flux.error(new Exception("It's not yours password!"));//error

            return sharedPasswordRepo.findAllByPasswordId(passId)
                    .flatMap(sharedPassword -> appUserRepo.findById(sharedPassword.getUserId()))
                    .map(userMapper::userToDTO);
        });
    }
}
