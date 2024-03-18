package ru.arbis29.passstorage.services;

import reactor.core.publisher.Mono;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface KeysService {
    String getPemKey(PublicKey key);
    String getPemKey(PrivateKey key);
    Mono<Boolean> existsById(String userId);

    Mono<Void> createKeyPare(String userId) throws GeneralSecurityException;
    Mono<byte[]> getPrivKey(String userId);
    Mono<String> getPubKey(String userId);

    Mono<byte[]> encryptPass(String userId, String pass);
    Mono<String> decryptPass(String userId, byte[] encryptedPass, String keyPass);
}
