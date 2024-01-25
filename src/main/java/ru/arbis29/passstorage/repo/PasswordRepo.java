package ru.arbis29.passstorage.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.domain.AppUser;
import ru.arbis29.passstorage.domain.StoredPassword;

public interface PasswordRepo extends ReactiveCrudRepository<StoredPassword,String> {
}
