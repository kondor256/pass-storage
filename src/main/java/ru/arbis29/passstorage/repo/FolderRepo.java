package ru.arbis29.passstorage.repo;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.domain.PasswordFolder;

public interface FolderRepo extends ReactiveCrudRepository<PasswordFolder,String> {
//    Flux<PasswordFolder> findAllById(String id);
    Flux<PasswordFolder> findAllByOrderByName();
    Flux<PasswordFolder> findAllByOwnerUserIdOrderByName(String userId);
    Mono<PasswordFolder> findFirstByIdAndOwnerUserId(String id, String userId);
    Mono<Void> deleteByIdAndOwnerUserId(String id, String userId);
    @Query("SELECT * FROM password_folders pf " +
            "INNER JOIN app_users au ON au.id = pf.owner_user_id " +
            "AND au.login=:login " +
            "ORDER BY pf.name")
    Flux<PasswordFolder> findAllByLoginOrderByName(String login);
    @Query("SELECT * FROM password_folders " +
            "INNER JOIN app_users " +
            "ON app_users.id = password_folders.owner_user_id " +
            "AND password_folders.id=:id AND app_users.login=:login"
    )
    Mono<PasswordFolder> findFirstByIdAndLogin(String id, String login);

    @Query("DELETE from password_folders " +
            "USING app_users " +
            "WHERE owner_user_id = app_users.id " +
            "AND app_users.login=:login " +
            "AND password_folders.id=:id")
    Mono<Void> deleteByIdAndLogin(String id, String login);
}
