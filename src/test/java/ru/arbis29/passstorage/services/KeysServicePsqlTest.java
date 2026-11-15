package ru.arbis29.passstorage.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.arbis29.passstorage.domain.UsersKeys;
import ru.arbis29.passstorage.repo.KeysRepo;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeysServicePsqlTest {

    @Mock
    private KeysRepo keysRepo;

    @InjectMocks
    private KeysServicePsql keysService;

    private final String testUserId = "test-user-id";
    private KeyPair testKeyPair;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        testKeyPair = generator.generateKeyPair();
    }

    @Test
    void getPemKey_WithPublicKey_ReturnsPemFormattedString() {
        // Arrange
        PublicKey publicKey = testKeyPair.getPublic();

        // Act
        String result = keysService.getPemKey(publicKey);

        // Assert
        assert result.startsWith("-----BEGIN PUBLIC KEY-----");
        assert result.endsWith("-----END PUBLIC KEY-----");
        assert result.contains("MII"); // Base64 encoded key content
    }

    @Test
    void getPemKey_WithPrivateKey_ReturnsPemFormattedString() {
        // Arrange
        PrivateKey privateKey = testKeyPair.getPrivate();

        // Act
        String result = keysService.getPemKey(privateKey);

        // Assert
        assert result.startsWith("-----BEGIN PRIVATE KEY-----");
        assert result.endsWith("-----END PRIVATE KEY-----");
        assert result.contains("MII"); // Base64 encoded key content
    }

    @Test
    void existsById_WhenKeyExists_ReturnsTrue() {
        // Arrange
        when(keysRepo.existsById(testUserId)).thenReturn(Mono.just(true));

        // Act
        Mono<Boolean> result = keysService.existsById(testUserId);

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(keysRepo).existsById(testUserId);
    }

    @Test
    void existsById_WhenKeyDoesNotExist_ReturnsFalse() {
        // Arrange
        when(keysRepo.existsById(testUserId)).thenReturn(Mono.just(false));

        // Act
        Mono<Boolean> result = keysService.existsById(testUserId);

        // Assert
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(keysRepo).existsById(testUserId);
    }

    @Test
    void createKeyPare_WhenCalled_CreatesAndSavesKeyPair() throws Exception {
        // Arrange
        when(keysRepo.save(any(UsersKeys.class))).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = keysService.createKeyPare(testUserId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(keysRepo).save(any(UsersKeys.class));
    }

    @Test
    void getPrivKey_WhenKeyExists_ReturnsPrivateKeyBytes() {
        // Arrange
        byte[] expectedPrivKey = testKeyPair.getPrivate().getEncoded();
        when(keysRepo.findById(testUserId)).thenReturn(Mono.just(new UsersKeys(testUserId, expectedPrivKey, new byte[0], true)));

        // Act
        Mono<byte[]> result = keysService.getPrivKey(testUserId);

        // Assert
        StepVerifier.create(result)
                .expectNext(expectedPrivKey)
                .verifyComplete();

        verify(keysRepo).findById(testUserId);
    }

    @Test
    void getPrivKey_WhenKeyDoesNotExist_ReturnsEmpty() {
        // Arrange
        when(keysRepo.findById(testUserId)).thenReturn(Mono.empty());

        // Act
        Mono<byte[]> result = keysService.getPrivKey(testUserId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(keysRepo).findById(testUserId);
    }

    @Test
    void getPubKey_WhenKeyExists_ReturnsPemFormattedPublicKey() {
        // Arrange
        byte[] pubKeyBytes = testKeyPair.getPublic().getEncoded();
        when(keysRepo.findById(testUserId)).thenReturn(Mono.just(new UsersKeys(testUserId, new byte[0], pubKeyBytes, true)));

        // Act
        Mono<String> result = keysService.getPubKey(testUserId);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(pem -> pem.startsWith("-----BEGIN PUBLIC KEY-----") && pem.endsWith("-----END PUBLIC KEY-----"))
                .verifyComplete();

        verify(keysRepo).findById(testUserId);
    }

    @Test
    void getPubKey_WhenKeyDoesNotExist_ReturnsEmpty() {
        // Arrange
        when(keysRepo.findById(testUserId)).thenReturn(Mono.empty());

        // Act
        Mono<String> result = keysService.getPubKey(testUserId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(keysRepo).findById(testUserId);
    }

    @Test
    void encryptPass_WhenKeyExists_ReturnsEncryptedBytes() {
        // Arrange
        String password = "test-password";
        byte[] privKeyBytes = testKeyPair.getPrivate().getEncoded();
        when(keysRepo.findById(testUserId)).thenReturn(Mono.just(new UsersKeys(testUserId, privKeyBytes, new byte[0], true)));

        // Act
        Mono<byte[]> result = keysService.encryptPass(testUserId, password);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(encrypted -> encrypted.length > 0)
                .verifyComplete();

        verify(keysRepo).findById(testUserId);
    }

    @Test
    void encryptPass_WhenKeyDoesNotExist_ReturnsError() {
        // Arrange
        when(keysRepo.findById(testUserId)).thenReturn(Mono.empty());

        // Act
        Mono<byte[]> result = keysService.encryptPass(testUserId, "test-password");

        // Assert
        StepVerifier.create(result)
                .expectError()
                .verify();

        verify(keysRepo).findById(testUserId);
    }

    @Test
    void decryptPass_WhenKeyExists_ReturnsDecryptedString() {
        // Arrange
        String originalPassword = "test-password";
        byte[] pubKeyBytes = testKeyPair.getPublic().getEncoded();
        // Encrypt the password with the private key to simulate the encrypted pass
        byte[] encryptedPass;
        try {
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            java.security.spec.PKCS8EncodedKeySpec keySpec = new java.security.spec.PKCS8EncodedKeySpec(testKeyPair.getPrivate().getEncoded());
            java.security.PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA");
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, privateKey);
            encryptedPass = cipher.doFinal(originalPassword.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(keysRepo.findById(testUserId)).thenReturn(Mono.just(new UsersKeys(testUserId, new byte[0], pubKeyBytes, true)));

        // Act
        Mono<String> result = keysService.decryptPass(testUserId, encryptedPass, "");

        // Assert
        StepVerifier.create(result)
                .expectNext(originalPassword)
                .verifyComplete();

        verify(keysRepo).findById(testUserId);
    }

    @Test
    void decryptPass_WhenKeyDoesNotExist_ReturnsError() {
        // Arrange
        when(keysRepo.findById(testUserId)).thenReturn(Mono.empty());

        // Act
        Mono<String> result = keysService.decryptPass(testUserId, new byte[0], "");

        // Assert
        StepVerifier.create(result)
                .expectError()
                .verify();

        verify(keysRepo).findById(testUserId);
    }
}