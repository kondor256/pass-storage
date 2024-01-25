package ru.arbis29.passstorage.services;

import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.mappers.PasswordMapper;
import ru.arbis29.passstorage.model.PasswordDTO;
import ru.arbis29.passstorage.repo.AppUserRepo;
import ru.arbis29.passstorage.repo.PasswordRepo;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordServicePsql implements PasswordService {

    private final AppUserRepo appUserRepo;
    private final PasswordRepo passwordRepo;
    private final AppUserRepo userRepo;
    private final PasswordMapper passwordMapper;

    @Override
    public Flux<PasswordDTO> listPasswords() {
        return passwordRepo.findAll()
                .map(passwordMapper::passToPassDTO);
    }

    @Override
    public Mono<PasswordDTO> getPassById(String id) {

        return passwordRepo.findById(id)
                .map(passwordMapper::passToPassDTO);
    }

    @Override
    public Mono<PasswordDTO> saveNewPass(PasswordDTO passwordDTO) {
        if (StringUtil.isNullOrEmpty(passwordDTO.getId())){
            passwordDTO.setId(UUID.randomUUID().toString());
        }

//        return passwordRepo.findById(passwordDTO.getId())
//                .map(foundPass -> passwordMapper.passDTOToPass(passwordDTO))
//                .flatMap(passwordRepo::save)
//                .map(passwordMapper::passToPassDTO);

        return passwordRepo.save(passwordMapper.passDTOToPass(passwordDTO))
                .map(passwordMapper::passToPassDTO);
    }
}
