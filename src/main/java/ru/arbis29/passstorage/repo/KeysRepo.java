package ru.arbis29.passstorage.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.domain.UsersKeys;

public interface KeysRepo extends ReactiveCrudRepository<UsersKeys, String> {
//    Mono<Boolean> existsByUserId(String userId);
}
