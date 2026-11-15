package ru.arbis29.passstorage.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServicePsqlTest {

    @Mock
    private AppUserRepo appUserRepo;
    @Mock
    private PasswordRepo passwordRepo;
    @Mock
    private SharedPasswordRepo sharedPasswordRepo;
    @Mock
    private PasswordMapper passwordMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserService userService;
    @Mock
    private KeysService keysService;

    @InjectMocks
    private PasswordServicePsql passwordService;

    private Principal mockPrincipal;
    private String testUserId;

    @BeforeEach
    void setUp() {
        mockPrincipal = mock(Principal.class);
        testUserId = "test-user-id";
        when(userService.getUserId(mockPrincipal)).thenReturn(testUserId);
    }

    // --- listPasswords ---

    @Test
    void listPasswords_ShouldReturnOwnAndSharedPasswords() {
        // Arrange
        StoredPassword ownPass = createStoredPassword("1", "Own Pass", testUserId, null, "decrypted");
        PasswordDTO ownDto = createPasswordDTO("1", "Own Pass", testUserId, false);
        when(passwordMapper.passToPassDTO(ownPass)).thenReturn(ownDto);
        when(passwordRepo.findAllByOwnerUserId(testUserId)).thenReturn(Flux.just(ownPass));

        SharedPassword sharedEntry = SharedPassword.builder()
                .userId(testUserId)
                .passwordId("2")
                .encryptedPassword("shared-enc".getBytes())
                .build();
        StoredPassword sharedStoredPass = createStoredPassword("2", "Shared Pass", "other-user", "shared-enc".getBytes(), null);
        PasswordDTO sharedDto = createPasswordDTO("2", "Shared Pass", "other-user", true);
        when(passwordMapper.passToPassDTO(sharedStoredPass)).thenReturn(sharedDto);
        when(sharedPasswordRepo.findAllByUserId(testUserId)).thenReturn(Flux.just(sharedEntry));
        when(passwordRepo.findById("2")).thenReturn(Mono.just(sharedStoredPass));
        when(keysService.decryptPass(eq(testUserId), eq("shared-enc".getBytes()), isNull()))
                .thenReturn(Mono.just("shared-decrypted"));

        // Act
        Flux<PasswordDTO> result = passwordService.listPasswords(mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getId().equals("1") && !dto.isShared())
                .expectNextMatches(dto -> dto.getId().equals("2") && dto.isShared())
                .verifyComplete();
    }

    @Test
    void listPasswords_WhenNoPasswords_ShouldReturnEmptyFlux() {
        // Arrange
        when(passwordRepo.findAllByOwnerUserId(testUserId)).thenReturn(Flux.empty());
        when(sharedPasswordRepo.findAllByUserId(testUserId)).thenReturn(Flux.empty());

        // Act
        Flux<PasswordDTO> result = passwordService.listPasswords(mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void listPasswords_WithEncryptedPassword_ShouldDecrypt() {
        // Arrange
        StoredPassword encryptedPass = createStoredPassword("1", "Encrypted Pass", testUserId, "encrypted".getBytes(), null);
        PasswordDTO decryptedDto = createPasswordDTO("1", "Encrypted Pass", testUserId, false);
        when(passwordMapper.passToPassDTO(any())).thenReturn(decryptedDto);
        when(passwordRepo.findAllByOwnerUserId(testUserId)).thenReturn(Flux.just(encryptedPass));
        when(keysService.decryptPass(eq(testUserId), eq("encrypted".getBytes()), isNull()))
                .thenReturn(Mono.just("decrypted"));
        when(sharedPasswordRepo.findAllByUserId(testUserId)).thenReturn(Flux.empty());

        // Act
        Flux<PasswordDTO> result = passwordService.listPasswords(mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getId().equals("1") && !dto.isShared())
                .verifyComplete();

        verify(keysService).decryptPass(testUserId, "encrypted".getBytes(), null);
    }

    // --- getPassById ---

    @Test
    void getPassById_WhenOwner_ShouldReturnPassword() {
        // Arrange
        String passId = "pass-123";
        StoredPassword storedPass = createStoredPassword(passId, "My Pass", testUserId, null, "plaintext");
        PasswordDTO dto = createPasswordDTO(passId, "My Pass", testUserId, false);
        when(passwordRepo.findById(passId)).thenReturn(Mono.just(storedPass));
        when(passwordMapper.passToPassDTO(storedPass)).thenReturn(dto);

        // Act
        Mono<PasswordDTO> result = passwordService.getPassById(passId, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .expectNext(dto)
                .verifyComplete();

        verify(passwordRepo).findById(passId);
        verify(passwordMapper).passToPassDTO(storedPass);
    }

    @Test
    void getPassById_WhenNotOwner_ShouldReturnEmpty() {
        // Arrange
        String passId = "pass-123";
        StoredPassword storedPass = createStoredPassword(passId, "Other Pass", "other-user", null, "plaintext");
        when(passwordRepo.findById(passId)).thenReturn(Mono.just(storedPass));

        // Act
        Mono<PasswordDTO> result = passwordService.getPassById(passId, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(passwordRepo).findById(passId);
        verifyNoInteractions(passwordMapper);
    }

    @Test
    void getPassById_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        String passId = "non-existent";
        when(passwordRepo.findById(passId)).thenReturn(Mono.empty());

        // Act
        Mono<PasswordDTO> result = passwordService.getPassById(passId, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void getPassById_WhenEncrypted_ShouldDecrypt() {
        // Arrange
        String passId = "pass-enc";
        StoredPassword storedPass = createStoredPassword(passId, "Encrypted", testUserId, "enc-data".getBytes(), null);
        PasswordDTO dto = createPasswordDTO(passId, "Encrypted", testUserId, false);
        when(passwordRepo.findById(passId)).thenReturn(Mono.just(storedPass));
        when(passwordMapper.passToPassDTO(any())).thenReturn(dto);
        when(keysService.decryptPass(eq(testUserId), eq("enc-data".getBytes()), isNull()))
                .thenReturn(Mono.just("decrypted-value"));

        // Act
        Mono<PasswordDTO> result = passwordService.getPassById(passId, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .expectNext(dto)
                .verifyComplete();

        verify(keysService).decryptPass(testUserId, "enc-data".getBytes(), null);
    }

    // --- saveNewPass ---

    @Test
    void saveNewPass_WhenNoId_ShouldCreateNewPassword() {
        // Arrange
        PasswordDTO inputDto = createPasswordDTO(null, "New Pass", null, false);
        StoredPassword storedPass = StoredPassword.builder().name("New Pass").build();
        AppUser user = AppUser.builder().id(testUserId).build();
        StoredPassword savedPass = createStoredPassword("gen-id", "New Pass", testUserId, "encrypted".getBytes(), null);
        PasswordDTO savedDto = createPasswordDTO("gen-id", "New Pass", testUserId, false);

        when(passwordMapper.passDTOToPass(inputDto)).thenReturn(storedPass);
        when(appUserRepo.findById(testUserId)).thenReturn(Mono.just(user));
        when(keysService.encryptPass(testUserId, null)).thenReturn(Mono.just("encrypted".getBytes()));
        when(passwordRepo.save(storedPass)).thenReturn(Mono.just(savedPass));
        when(passwordMapper.passToPassDTO(savedPass)).thenReturn(savedDto);

        // Act
        Mono<PasswordDTO> result = passwordService.saveNewPass(inputDto, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .expectNext(savedDto)
                .verifyComplete();

        verify(appUserRepo).findById(testUserId);
        verify(passwordRepo).save(storedPass);
        assert storedPass.getOwnerUserId().equals(testUserId);
    }

    @Test
    void saveNewPass_WhenIdExistsAndOwner_ShouldUpdatePassword() {
        // Arrange
        String passId = "existing-id";
        PasswordDTO inputDto = createPasswordDTO(passId, "Updated Pass", null, false);
        StoredPassword existingPass = createStoredPassword(passId, "Old Pass", testUserId, "enc".getBytes(), null);
        StoredPassword updatedPass = createStoredPassword(passId, "Updated Pass", testUserId, "new-enc".getBytes(), null);
        PasswordDTO updatedDto = createPasswordDTO(passId, "Updated Pass", testUserId, false);

        when(passwordMapper.passDTOToPass(inputDto)).thenReturn(new StoredPassword());
        when(passwordRepo.findById(passId)).thenReturn(Mono.just(existingPass));
        when(keysService.encryptPass(eq(testUserId), any())).thenReturn(Mono.just("new-enc".getBytes()));
        when(passwordRepo.save(any())).thenReturn(Mono.just(updatedPass));
        when(passwordMapper.passToPassDTO(updatedPass)).thenReturn(updatedDto);

        // Act
        Mono<PasswordDTO> result = passwordService.saveNewPass(inputDto, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .expectNext(updatedDto)
                .verifyComplete();

        verify(passwordRepo).findById(passId);
        verify(passwordRepo).save(any());
    }

    @Test
    void saveNewPass_WhenIdExistsButNotOwner_ShouldReturnEmpty() {
        // Arrange
        String passId = "other-pass";
        PasswordDTO inputDto = createPasswordDTO(passId, "Other Pass", null, false);
        StoredPassword existingPass = createStoredPassword(passId, "Other Pass", "other-user", null, "data");
        when(passwordMapper.passDTOToPass(inputDto)).thenReturn(new StoredPassword());
        when(passwordRepo.findById(passId)).thenReturn(Mono.just(existingPass));

        // Act
        Mono<PasswordDTO> result = passwordService.saveNewPass(inputDto, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(passwordRepo).findById(passId);
        verifyNoMoreInteractions(passwordRepo);
    }

    // --- deletePassById ---

    @Test
    void deletePassById_WhenOwner_ShouldDeletePassword() {
        // Arrange
        String passId = "pass-to-delete";
        StoredPassword storedPass = createStoredPassword(passId, "ToDelete", testUserId, null, "data");
        when(passwordRepo.findById(passId)).thenReturn(Mono.just(storedPass));
        when(sharedPasswordRepo.deleteAllByPasswordId(passId)).thenReturn(Mono.empty());
        when(passwordRepo.deleteById(passId)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = passwordService.deletePassById(passId, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(sharedPasswordRepo).deleteAllByPasswordId(passId);
        verify(passwordRepo).deleteById(passId);
    }

    @Test
    void deletePassById_WhenNotOwner_ShouldDoNothing() {
        // Arrange
        String passId = "other-pass";
        StoredPassword storedPass = createStoredPassword(passId, "Other", "other-user", null, "data");
        when(passwordRepo.findById(passId)).thenReturn(Mono.just(storedPass));

        // Act
        Mono<Void> result = passwordService.deletePassById(passId, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(passwordRepo).findById(passId);
        verifyNoMoreInteractions(passwordRepo);
    }

    @Test
    void deletePassById_WhenNotExists_ShouldDoNothing() {
        // Arrange
        String passId = "non-existent";
        when(passwordRepo.findById(passId)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = passwordService.deletePassById(passId, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    // --- sharePass ---

    @Test
    void sharePass_WhenOwner_ShouldShareWithUsers() {
        // Arrange
        String passId = "pass-share";
        List<String> userIds = List.of("user-a", "user-b");
        StoredPassword storedPass = createStoredPassword(passId, "Shared Pass", testUserId, "enc".getBytes(), null);
        when(passwordRepo.findById(passId)).thenReturn(Mono.just(storedPass));
        when(keysService.decryptPass(eq(testUserId), eq("enc".getBytes()), isNull()))
                .thenReturn(Mono.just("decrypted"));
        when(keysService.encryptPass(eq("user-a"), eq("decrypted"))).thenReturn(Mono.just("enc-for-a".getBytes()));
        when(keysService.encryptPass(eq("user-b"), eq("decrypted"))).thenReturn(Mono.just("enc-for-b".getBytes()));
        when(sharedPasswordRepo.deleteAllByPasswordId(passId)).thenReturn(Mono.empty());
        when(sharedPasswordRepo.save(any())).thenReturn(Mono.just(SharedPassword.builder().build()));

        // Act
        Mono<Void> result = passwordService.sharePass(passId, userIds, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(sharedPasswordRepo).deleteAllByPasswordId(passId);
    }

    @Test
    void sharePass_WhenNotOwner_ShouldThrowException() {
        // Arrange
        String passId = "other-pass";
        StoredPassword storedPass = createStoredPassword(passId, "Other", "other-user", null, "data");
        when(passwordRepo.findById(passId)).thenReturn(Mono.just(storedPass));

        // Act
        Mono<Void> result = passwordService.sharePass(passId, List.of("user-a"), mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof Exception
                        && throwable.getMessage().equals("It's not yours password!"))
                .verify();
    }

    @Test
    void sharePass_WhenPasswordNotExists_ShouldDoNothing() {
        // Arrange
        String passId = "non-existent";
        when(passwordRepo.findById(passId)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = passwordService.sharePass(passId, List.of("user-a"), mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    // --- getSharedPassUsers ---

    @Test
    void getSharedPassUsers_WhenOwner_ShouldReturnSharedUsers() {
        // Arrange
        String passId = "pass-shared";
        StoredPassword storedPass = createStoredPassword(passId, "Shared", testUserId, null, "data");
        SharedPassword sharedEntry = SharedPassword.builder()
                .userId("shared-user-id")
                .passwordId(passId)
                .encryptedPassword("enc".getBytes())
                .build();
        AppUser sharedUser = AppUser.builder().id("shared-user-id").name("Shared User").login("shareduser").build();
        UserDTO userDto = UserDTO.builder().id("shared-user-id").name("Shared User").login("shareduser").build();

        when(passwordRepo.findById(passId)).thenReturn(Mono.just(storedPass));
        when(sharedPasswordRepo.findAllByPasswordId(passId)).thenReturn(Flux.just(sharedEntry));
        when(appUserRepo.findById("shared-user-id")).thenReturn(Mono.just(sharedUser));
        when(userMapper.userToDTO(sharedUser)).thenReturn(userDto);

        // Act
        Flux<UserDTO> result = passwordService.getSharedPassUsers(passId, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .expectNext(userDto)
                .verifyComplete();

        verify(userMapper).userToDTO(sharedUser);
    }

    @Test
    void getSharedPassUsers_WhenNotOwner_ShouldThrowException() {
        // Arrange
        String passId = "other-pass";
        StoredPassword storedPass = createStoredPassword(passId, "Other", "other-user", null, "data");
        when(passwordRepo.findById(passId)).thenReturn(Mono.just(storedPass));

        // Act
        Flux<UserDTO> result = passwordService.getSharedPassUsers(passId, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof Exception
                        && throwable.getMessage().equals("It's not yours password!"))
                .verify();
    }

    @Test
    void getSharedPassUsers_WhenNoSharedUsers_ShouldReturnEmpty() {
        // Arrange
        String passId = "pass-no-shares";
        StoredPassword storedPass = createStoredPassword(passId, "No Shares", testUserId, null, "data");
        when(passwordRepo.findById(passId)).thenReturn(Mono.just(storedPass));
        when(sharedPasswordRepo.findAllByPasswordId(passId)).thenReturn(Flux.empty());

        // Act
        Flux<UserDTO> result = passwordService.getSharedPassUsers(passId, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    // --- Helpers ---

    private StoredPassword createStoredPassword(String id, String name, String ownerUserId,
                                                 byte[] encryptedPassword, String password) {
        StoredPassword pass = new StoredPassword();
        pass.setId(id);
        pass.setName(name);
        pass.setOwnerUserId(ownerUserId);
        pass.setEncryptedPassword(encryptedPassword);
        pass.setPassword(password);
        return pass;
    }

    private PasswordDTO createPasswordDTO(String id, String name, String ownerUserId, boolean shared) {
        PasswordDTO dto = new PasswordDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setOwnerUserId(ownerUserId);
        dto.setShared(shared);
        return dto;
    }
}
