package ru.arbis29.passstorage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.domain.UsersKeys;
import ru.arbis29.passstorage.repo.KeysRepo;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeysServicePsql implements KeysService {
    private final KeysRepo keysRepo;

    private String getPemKey(byte[] publicKeyBytes, String type) {
        String publicKeyContent = Base64.getEncoder().encodeToString(publicKeyBytes);
        StringBuilder publicKeyFormatted = new StringBuilder("-----BEGIN " + type + " KEY-----" + System.lineSeparator());
        for (final String row:
                publicKeyContent.split("(?<=\\G.{64})")
        )
        {
            publicKeyFormatted.append(row).append(System.lineSeparator());
        }
        publicKeyFormatted.append("-----END ").append(type).append(" KEY-----");

        return publicKeyFormatted.toString();
    }
    @Override
    public String getPemKey(PublicKey key) {
        byte[] publicKeyBytes = key.getEncoded();
        return getPemKey(publicKeyBytes,"PUBLIC");
    }

    @Override
    public String getPemKey(PrivateKey key) {
        byte[] publicKeyBytes = key.getEncoded();
        return getPemKey(publicKeyBytes,"PRIVATE");
    }

    @Override
    public Mono<Boolean> existsById(String userId){
        return keysRepo.existsById(userId);
    }
    @Override
    public Mono<Void> createKeyPare(String userId) throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();

        String pem = getPemKey(pair.getPublic());
        log.warn(pem);
        pem = getPemKey(pair.getPrivate());
        log.warn(pem);

        String secretMessage = "5556360";
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, pair.getPrivate());
        byte[] secretMessageBytes = secretMessage.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
        String encodedMessage = Base64.getEncoder().encodeToString(encryptedMessageBytes);
        log.warn(encodedMessage);

        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, pair.getPublic());
        byte[] decryptedMessageBytes = decryptCipher.doFinal(encryptedMessageBytes);
        String decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
        log.warn(decryptedMessage);

        UsersKeys usersKeys = UsersKeys.builder()
                .userId(userId)
                .privKey(pair.getPrivate().getEncoded())
                .pubKey(pair.getPublic().getEncoded())
                .isNew(true)
                .build();

        return keysRepo.save(usersKeys).then();
    }

    @Override
    public Mono<byte[]> getPrivKey(String userId) {
        return keysRepo.findById(userId)
                .map(UsersKeys::getPrivKey);
    }

    @Override
    public Mono<String> getPubKey(String userId) {

        return keysRepo.findById(userId)
                .map(usersKeys -> getPemKey(usersKeys.getPubKey(),"PUBLIC"));
    }

    @Override
    public Mono<byte[]> encryptPass(String userId, String pass) {
        return keysRepo.findById(userId)
                .flatMap(usersKeys -> {
                    try {
                        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(usersKeys.getPrivKey());
                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

                        Cipher encryptCipher = Cipher.getInstance("RSA");
                        encryptCipher.init(Cipher.ENCRYPT_MODE, privateKey);
                        byte[] encryptedPass = encryptCipher.doFinal(pass.getBytes());

                        return Mono.just(encryptedPass);
                    } catch (GeneralSecurityException e) {
                        return Mono.error(new RuntimeException(e));
                    }
                });
    }

    @Override
    public Mono<String> decryptPass(String userId, byte[] encryptedPass, String keyPass) {
        return keysRepo.findById(userId)
                .flatMap(usersKeys -> {
                    try {
                        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(usersKeys.getPubKey());
                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                        PublicKey publicKey = keyFactory.generatePublic(keySpec);

                        Cipher decryptCypher = Cipher.getInstance("RSA");
                        decryptCypher.init(Cipher.DECRYPT_MODE, publicKey);
                        String decryptedPass = new String(decryptCypher.doFinal(encryptedPass), StandardCharsets.UTF_8);

                        return Mono.just(decryptedPass);
                    } catch (GeneralSecurityException e) {
                        return Mono.error(new RuntimeException(e));
                    }
                });

    }
}
