package ru.arbis29.passstorage.controllers;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.domain.AppUser;
import ru.arbis29.passstorage.mappers.UserMapper;
import ru.arbis29.passstorage.mappers.UserMapperImpl;
import ru.arbis29.passstorage.model.UserDTO;
import ru.arbis29.passstorage.repo.AppUserRepo;
import ru.arbis29.passstorage.services.UserService;
import ru.arbis29.passstorage.services.UserServicePsql;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = UserController.class)
@Import({UserServicePsql.class, UserMapperImpl.class})
@Slf4j
class UserControllerTest {
    @MockBean
    AppUserRepo appUserRepo;
    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
    }

    @Test
    void notAuthorized(){
        webTestClient.get().uri(UserController.BASE_URI)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = "ADMIN",username = "test_user")
    void getEmptyPass() {
        webTestClient.get()
                .uri(UserController.BASE_URI+"/empty")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN",username = "test_user")
    void getUserList() {

        Mockito.when(appUserRepo.findAll()).thenReturn(Flux.just(AppUser.builder().build()));

        webTestClient.get()
                .uri(UserController.BASE_URI+"/list")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDTO.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN",username = "test_user")
    void getCurrentUser() {
        AppUser currentUser = AppUser.builder()
                .id("test_user")
                .login("test_user")
                .build();
        assert currentUser.getId() != null;
        Mockito.when(appUserRepo.findById(currentUser.getId())).thenReturn(Mono.just(currentUser));

        UserDTO userDTO = webTestClient.get()
                .uri(UserController.BASE_URI+"/current")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .returnResult()
                .getResponseBody();

        assert userDTO != null;
        assertEquals(userDTO.getId(),currentUser.getId());
    }
}