package ru.arbis29.passstorage.services;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.model.UserDTO;

import java.security.Principal;

public interface UserService {
    UserDTO getUser(LdapUserDetails userDetails);
    String getUserId(Principal principal);

    Flux<UserDTO> listUsers();
    Mono<UserDTO> getUserById(String id);
    Mono<UserDTO> getUserByLogin(String login);
    Mono<UserDTO> getUser(Principal principal);
    Mono<UserDTO> saveUser(UserDTO user);
    Mono<Void> deleteUser(UserDTO user);

    Mono<Void> createKeyPare(UserDTO user);
    Mono<String> getPrivateKey(UserDTO user);
}
