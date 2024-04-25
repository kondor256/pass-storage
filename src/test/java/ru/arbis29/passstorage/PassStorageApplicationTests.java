package ru.arbis29.passstorage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.arbis29.passstorage.domain.SharedPassword;
import ru.arbis29.passstorage.repo.SharedPasswordRepo;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class PassStorageApplicationTests {
    @Autowired
    SharedPasswordRepo sharedPasswordRepo;

    @Test
    void contextLoads() {
    }

    @Test
    void testUpdateSharedPass(){
        List<SharedPassword> passwordList = new ArrayList<>();
        passwordList.add(SharedPassword.builder()
                        .userId("u1")
                        .passwordId("p1")
                        .encryptedPassword("akjsdhakjsd".getBytes(StandardCharsets.UTF_8))
                .build());
        passwordList.add(SharedPassword.builder()
                .userId("u2")
                .passwordId("p1")
                .encryptedPassword("asdasdafasasd".getBytes(StandardCharsets.UTF_8))
                .build());

//        sharedPasswordRepo.updateSharedPasswordUsers("p1",passwordList).block();

    }
}
