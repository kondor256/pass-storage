package ru.arbis29.passstorage.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.arbis29.passstorage.domain.StoredPassword;

public interface PasswordRepo extends ReactiveCrudRepository<StoredPassword,String> {
    Flux<StoredPassword> findAllByOwnerUserId(String userId);
}
