package ru.arbis29.passstorage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.model.PasswordDTO;
import ru.arbis29.passstorage.model.UserDTO;
import ru.arbis29.passstorage.services.PasswordService;
import ru.arbis29.passstorage.services.UserService;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@Slf4j
public class userController {
    private static final String BASE_URI = "/api/v1/user";

    private final UserService userService;

    @GetMapping(BASE_URI + "/empty")
    Mono<UserDTO> getEmptyPass(Principal principal){
        return Mono.just(UserDTO.builder().build());
    }
    @GetMapping(BASE_URI + "/list")
    Flux<UserDTO> getUserList(Principal principal){
        return userService.listUsers();
    }

    @GetMapping(BASE_URI+"/current")
    Mono<UserDTO> getCurrentUser(Principal principal){
        return userService.getUser(principal);
    }
}
