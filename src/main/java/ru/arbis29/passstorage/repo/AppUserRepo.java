package ru.arbis29.passstorage.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.domain.AppUser;

public interface AppUserRepo extends ReactiveCrudRepository<AppUser,String> {
    Flux<AppUser> findAllByLogin(String login);
}
