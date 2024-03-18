package ru.arbis29.passstorage.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.arbis29.passstorage.model.UserDTO;
import ru.arbis29.passstorage.services.KeysService;
import ru.arbis29.passstorage.services.UserService;

import java.security.GeneralSecurityException;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationEvents {
    private final UserService userService;
    private final KeysService keysService;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        LdapUserDetails userDetails = (LdapUserDetails) success.getAuthentication().getPrincipal();

        UserDTO userDTO = userService.getUser(userDetails);
        userService.saveUser(userDTO)
                .flatMap(userDTO1 -> keysService.existsById(userDTO.getId())
                        .flatMap(exist -> {
                            try {
                                return exist?Mono.empty():
                                        keysService.createKeyPare(userDTO.getId());
                            } catch (GeneralSecurityException e) {
                                return Mono.error(new RuntimeException(e));
                            }
                        })).subscribe();
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failures) {
        log.warn(failures.toString());
    }
}
