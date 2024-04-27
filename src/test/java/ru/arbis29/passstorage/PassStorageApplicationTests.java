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
    @Test
    void contextLoads() {
    }
}
