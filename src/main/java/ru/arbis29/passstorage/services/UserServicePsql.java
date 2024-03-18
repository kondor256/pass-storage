package ru.arbis29.passstorage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.domain.AppUser;
import ru.arbis29.passstorage.mappers.UserMapper;
import ru.arbis29.passstorage.model.UserDTO;
import ru.arbis29.passstorage.repo.AppUserRepo;

import java.security.Principal;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServicePsql implements UserService {
    private final AppUserRepo userRepo;
    private final UserMapper userMapper;

    @Override
    public UserDTO getUser(LdapUserDetails userDetails) {
        String name = userDetails.getDn();
        String login = userDetails.getUsername().toLowerCase();
        StringBuilder userId = new StringBuilder();
        String[] sa = name.split(",");
        for (String s : sa) {
            if (s.startsWith("CN=")) name = s.substring(3);
            if (s.startsWith("DC=")) userId.append((userId.length() == 0) ? "" : ".").append(s.substring(3));
        }
        userId.append((userId.length() == 0) ? "" : "/").append(login);

        return UserDTO.builder()
                .id(userId.toString())
                .login(login)
                .name(name)
                .build();
    }

    @Override
    public String getUserId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken){
            Object userDetails = ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
            if (userDetails instanceof LdapUserDetails){
                return getUser((LdapUserDetails) userDetails).getId();
            } else return null;
        }
        return null;
    }

    @Override
    public Flux<UserDTO> listUsers() {
        return userRepo.findAll()
                .map(userMapper::userToDTO);
    }

    @Override
    public Mono<UserDTO> getUserById(String id) {
        return userRepo.findById(id)
                .map(userMapper::userToDTO);
    }

    @Override
    public Mono<UserDTO> getUserByLogin(String login) {
        return userRepo.findAllByLogin(login)
                .singleOrEmpty()
                .map(userMapper::userToDTO);
    }

    @Override
    public Mono<UserDTO> saveUser(UserDTO user) {
        final String userId = user.getId();
        final String name = user.getName();
        final String login = user.getLogin();
        return userRepo.findById(userId)
                .switchIfEmpty(Mono.just(AppUser.builder()
                        .id(userId)
                        .isNew(true)
                        .build()))
                .flatMap(foundUser -> {
                    foundUser.setLogin(login);
                    foundUser.setName(name);
                    return userRepo.save(foundUser)
                            .map(userMapper::userToDTO);
                    });
    }

    @Override
    public Mono<Void> deleteUser(UserDTO user) {
        return null;
    }

    @Override
    public Mono<Void> createKeyPare(UserDTO user) {
        return null;
    }

    @Override
    public Mono<String> getPrivateKey(UserDTO user) {
        return null;
    }
}
