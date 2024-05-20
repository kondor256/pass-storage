package ru.arbis29.passstorage.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
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
import ru.arbis29.passstorage.domain.PasswordFolder;
import ru.arbis29.passstorage.domain.SharedPassword;
import ru.arbis29.passstorage.domain.StoredPassword;
import ru.arbis29.passstorage.mappers.FolderMapper;
import ru.arbis29.passstorage.mappers.FolderMapperImpl;
import ru.arbis29.passstorage.mappers.UserMapperImpl;
import ru.arbis29.passstorage.model.FolderDTO;
import ru.arbis29.passstorage.repo.AppUserRepo;
import ru.arbis29.passstorage.repo.FolderRepo;
import ru.arbis29.passstorage.repo.PasswordRepo;
import ru.arbis29.passstorage.repo.SharedPasswordRepo;
import ru.arbis29.passstorage.services.FolderServicePsql;
import ru.arbis29.passstorage.services.UserServicePsql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = FolderController.class)
@Import({FolderServicePsql.class, FolderMapperImpl.class, UserServicePsql.class, UserMapperImpl.class})
@Slf4j
class FolderControllerTest {
    @Autowired
    WebTestClient webTestClient;
    @Autowired
    FolderMapper folderMapper;

    @MockBean
    FolderRepo folderRepo;
    @MockBean
    AppUserRepo userRepo;
    @MockBean
    PasswordRepo passwordRepo;
    @MockBean
    SharedPasswordRepo sharedPasswordRepo;

    static final String TEST_USER_ID = "test_user";
    static final String TEST_USER1_ID = "test_user1";
    static Map<String, AppUser> users = new HashMap<>();
    static Map<String, PasswordFolder> folders = new HashMap<>(), folders1 = new HashMap<>();
    static Map<String, StoredPassword> passwords = new HashMap<>(), passwords1 = new HashMap<>();
    static Map<String, SharedPassword> sharedPasswords = new HashMap<>();

    @BeforeAll
    static void prepareData(){
        users.put(TEST_USER_ID, AppUser.builder() .id(TEST_USER_ID) .login(TEST_USER_ID) .name(TEST_USER_ID) .build());
        users.put(TEST_USER1_ID, AppUser.builder().id(TEST_USER1_ID).login(TEST_USER1_ID).name(TEST_USER1_ID).build());

        folders .put("1",PasswordFolder.builder().id("1").ownerUserId(TEST_USER_ID) .build());
        folders .put("2",PasswordFolder.builder().id("2").ownerUserId(TEST_USER_ID) .build());
        folders1.put("3",PasswordFolder.builder().id("3").ownerUserId(TEST_USER1_ID).build());
        folders1.put("4",PasswordFolder.builder().id("4").ownerUserId(TEST_USER1_ID).build());

        passwords .put("1", StoredPassword.builder().id("1").ownerUserId(TEST_USER_ID) .folderId("1").build());
        passwords .put("2", StoredPassword.builder().id("2").ownerUserId(TEST_USER_ID) .folderId("2").build());
        passwords .put("3", StoredPassword.builder().id("3").ownerUserId(TEST_USER_ID) .folderId("2").build());
        passwords1.put("4", StoredPassword.builder().id("4").ownerUserId(TEST_USER1_ID).folderId("3").build());
        passwords1.put("5", StoredPassword.builder().id("5").ownerUserId(TEST_USER1_ID).folderId("4").build());
        passwords1.put("6", StoredPassword.builder().id("6").ownerUserId(TEST_USER1_ID).folderId("4").build());

        sharedPasswords.put("4", SharedPassword.builder().passwordId("4").userId(TEST_USER_ID).build());
    }

    @Test
    void notAuthorized(){
        webTestClient.get().uri(FolderController.BASE_URI)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = "ADMIN",username = TEST_USER_ID)
    void getEmptyFolder() {
        webTestClient.get()
                .uri(FolderController.BASE_URI+"/empty")
                .exchange()
                .expectStatus().isOk()
                .expectBody(FolderDTO.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN",username = TEST_USER_ID)
    void getFolderList() {
        Mockito.when(folderRepo.findAllByOwnerUserIdOrderByName(TEST_USER_ID)).thenReturn(Flux.fromIterable(folders.values()));
        Mockito.when(folderRepo.findAllById(Mockito.anyList())).thenAnswer(invocationOnMock -> {
            List<String> id = invocationOnMock.getArgument(0, List.class);
            return Flux.fromIterable(id.stream().map(folders::get).filter(Objects::nonNull).toList())
                    .concatWith(Flux.fromIterable(id.stream().map(folders1::get).filter(Objects::nonNull).toList()));
        });
        Mockito.when(passwordRepo.findById(Mockito.anyString())).thenAnswer(invocationOnMock -> {
            String id = invocationOnMock.getArgument(0, String.class);
            StoredPassword password = passwords.get(id);
            if (password == null) password = passwords1.get(id);
            return Mono.just(password);
        });
        Mockito.when(sharedPasswordRepo.findAllByUserId(TEST_USER_ID)).thenReturn(Flux.fromIterable(sharedPasswords.values()));
        Mockito.when(userRepo.findAllById(Mockito.anyList())).thenAnswer(invocationOnMock -> {
            List<String> id = invocationOnMock.getArgument(0, List.class);
            return Flux.fromIterable(id.stream().map(users::get).filter(Objects::nonNull).toList());
        });

        webTestClient.get()
                .uri(FolderController.BASE_URI+"/list")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FolderDTO.class).hasSize(4).contains(folderMapper.fldToDTO(folders.get("1")));
    }

    @Test
    @WithMockUser(roles = "ADMIN",username = TEST_USER_ID)
    void saveFolder() {
    }

    @Test
    @WithMockUser(roles = "ADMIN",username = TEST_USER_ID)
    void deleteFolder() {
    }
}