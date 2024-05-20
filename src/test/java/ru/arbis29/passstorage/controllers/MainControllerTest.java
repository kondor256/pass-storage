package ru.arbis29.passstorage.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = MainController.class)
@Slf4j
class MainControllerTest {
    @Autowired
    WebTestClient webTestClient;

    @Test
    void notAuthorized(){
        webTestClient.get().uri("/")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = "ADMIN",username = "test_user")
    void rootApp() {
        webTestClient.get().uri("/")
                .exchange()
                .expectStatus().isOk();
    }
}