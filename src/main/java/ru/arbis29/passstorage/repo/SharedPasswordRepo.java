package ru.arbis29.passstorage.repo;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.domain.SharedPassword;

import java.util.List;

public interface SharedPasswordRepo extends ReactiveCrudRepository<SharedPassword,String> {
    Flux<SharedPassword> findAllByPasswordId(String passwordId);
    Flux<SharedPassword> findAllByUserId(String userId);

    Mono<Void> deleteAllByPasswordId(String passwordId);
}
