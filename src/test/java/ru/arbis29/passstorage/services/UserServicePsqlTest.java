package ru.arbis29.passstorage.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.arbis29.passstorage.domain.AppUser;
import ru.arbis29.passstorage.mappers.UserMapper;
import ru.arbis29.passstorage.model.UserDTO;
import ru.arbis29.passstorage.repo.AppUserRepo;

import java.security.Principal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServicePsqlTest {

    @Mock
    private AppUserRepo userRepo;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServicePsql userService;

    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockPrincipal = mock(Principal.class);
    }

    // --- getUser(LdapUserDetails) ---

    @Test
    void getUser_WithStandardLdapDn_ShouldConstructUserDTO() {
        // Arrange
        LdapUserDetails ldapDetails = mock(LdapUserDetails.class);
        when(ldapDetails.getDn()).thenReturn("CN=John Doe,CN=Users,DC=example,DC=com");
        when(ldapDetails.getUsername()).thenReturn("jdoe");

        // Act
        UserDTO result = userService.getUser(ldapDetails);

        // Assert
        StepVerifier.create(Mono.just(result))
                .expectNextMatches(dto ->
                        dto.getId().equals("example.com/jdoe") &&
                                dto.getLogin().equals("jdoe") &&
                                dto.getName().equals("John Doe"))
                .verifyComplete();
    }

    @Test
    void getUser_WithComplexLdapDn_ShouldParseCorrectly() {
        // Arrange
        LdapUserDetails ldapDetails = mock(LdapUserDetails.class);
        when(ldapDetails.getDn()).thenReturn("CN=Jane Smith,OU=Department,DC=company,DC=local");
        when(ldapDetails.getUsername()).thenReturn("jsmith");

        // Act
        UserDTO result = userService.getUser(ldapDetails);

        // Assert
        StepVerifier.create(Mono.just(result))
                .expectNextMatches(dto ->
                        dto.getId().equals("company.local/jsmith") &&
                                dto.getLogin().equals("jsmith") &&
                                dto.getName().equals("Jane Smith"))
                .verifyComplete();
    }

    @Test
    void getUser_WithMinimalLdapDn_ShouldHandleCorrectly() {
        // Arrange
        LdapUserDetails ldapDetails = mock(LdapUserDetails.class);
        when(ldapDetails.getDn()).thenReturn("CN=Alice");
        when(ldapDetails.getUsername()).thenReturn("alice");

        // Act
        UserDTO result = userService.getUser(ldapDetails);

        // Assert
        StepVerifier.create(Mono.just(result))
                .expectNextMatches(dto ->
                        dto.getId().equals("alice") &&
                                dto.getLogin().equals("alice") &&
                                dto.getName().equals("Alice"))
                .verifyComplete();
    }

    // --- getUserId(Principal) ---

    @Test
    void getUserId_WithUsernamePasswordAuthenticationTokenContainingLdap_ShouldReturnUserId() {
        // Arrange
        LdapUserDetails ldapDetails = mock(LdapUserDetails.class);
        when(ldapDetails.getDn()).thenReturn("CN=User,CN=Users,DC=test,DC=com");
        when(ldapDetails.getUsername()).thenReturn("testuser");

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(ldapDetails, "password");

        // Act
        String result = userService.getUserId(authToken);

        // Assert
        assert result != null && result.equals("test.com/testuser");
    }

    @Test
    void getUserId_WithUsernamePasswordAuthenticationTokenContainingSpringUser_ShouldReturnUsername() {
        // Arrange
        UserDetails springUser = User.withUsername("springuser").password("pass").build();
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(springUser, "password");

        // Act
        String result = userService.getUserId(authToken);

        // Assert
        assert result != null && result.equals("springuser");
    }

    @Test
    void getUserId_WithNonAuthPrincipal_ShouldReturnNull() {
        // Arrange
        // A plain Principal is not a UsernamePasswordAuthenticationToken

        // Act
        String result = userService.getUserId(mockPrincipal);

        // Assert
        assert result == null;
    }

    // --- listUsers ---

    @Test
    void listUsers_ShouldReturnAllUsersAsDTOs() {
        // Arrange
        AppUser user1 = AppUser.builder().id("1").name("User One").login("userone").build();
        AppUser user2 = AppUser.builder().id("2").name("User Two").login("usertwo").build();
        UserDTO dto1 = UserDTO.builder().id("1").name("User One").login("userone").build();
        UserDTO dto2 = UserDTO.builder().id("2").name("User Two").login("usertwo").build();

        when(userRepo.findAll()).thenReturn(Flux.just(user1, user2));
        when(userMapper.userToDTO(user1)).thenReturn(dto1);
        when(userMapper.userToDTO(user2)).thenReturn(dto2);

        // Act
        Flux<UserDTO> result = userService.listUsers();

        // Assert
        StepVerifier.create(result)
                .expectNext(dto1, dto2)
                .verifyComplete();

        verify(userRepo).findAll();
        verify(userMapper).userToDTO(user1);
        verify(userMapper).userToDTO(user2);
    }

    @Test
    void listUsers_WhenNoUsers_ShouldReturnEmptyFlux() {
        // Arrange
        when(userRepo.findAll()).thenReturn(Flux.empty());

        // Act
        Flux<UserDTO> result = userService.listUsers();

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    // --- getUserById ---

    @Test
    void getUserById_WhenExists_ShouldReturnUserDTO() {
        // Arrange
        String userId = "user-123";
        AppUser appUser = AppUser.builder().id(userId).name("Test User").login("testuser").build();
        UserDTO dto = UserDTO.builder().id(userId).name("Test User").login("testuser").build();

        when(userRepo.findById(userId)).thenReturn(Mono.just(appUser));
        when(userMapper.userToDTO(appUser)).thenReturn(dto);

        // Act
        Mono<UserDTO> result = userService.getUserById(userId);

        // Assert
        StepVerifier.create(result)
                .expectNext(dto)
                .verifyComplete();

        verify(userRepo).findById(userId);
        verify(userMapper).userToDTO(appUser);
    }

    @Test
    void getUserById_WhenNotFound_ShouldReturnEmpty() {
        // Arrange
        String userId = "non-existent";
        when(userRepo.findById(userId)).thenReturn(Mono.empty());

        // Act
        Mono<UserDTO> result = userService.getUserById(userId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    // --- getUserByLogin ---

    @Test
    void getUserByLogin_WhenFound_ShouldReturnUserDTO() {
        // Arrange
        String login = "testlogin";
        AppUser appUser = AppUser.builder().id("uid-1").name("Test Login User").login(login).build();
        UserDTO dto = UserDTO.builder().id("uid-1").name("Test Login User").login(login).build();

        when(userRepo.findAllByLogin(login)).thenReturn(Flux.just(appUser));
        when(userMapper.userToDTO(appUser)).thenReturn(dto);

        // Act
        Mono<UserDTO> result = userService.getUserByLogin(login);

        // Assert
        StepVerifier.create(result)
                .expectNext(dto)
                .verifyComplete();

        verify(userRepo).findAllByLogin(login);
        verify(userMapper).userToDTO(appUser);
    }

    @Test
    void getUserByLogin_WhenNotFound_ShouldReturnEmpty() {
        // Arrange
        String login = "nonexistent";
        when(userRepo.findAllByLogin(login)).thenReturn(Flux.empty());

        // Act
        Mono<UserDTO> result = userService.getUserByLogin(login);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    // --- getUser(Principal) ---

    @Test
    void getUser_WithAuthPrincipal_ShouldReturnCurrentUser() {
        // Arrange
        LdapUserDetails ldapDetails = mock(LdapUserDetails.class);
        when(ldapDetails.getDn()).thenReturn("CN=CurrentUser,CN=Users,DC=test,DC=com");
        when(ldapDetails.getUsername()).thenReturn("currentuser");

        AppUser appUser = AppUser.builder()
                .id("test.com/currentuser")
                .name("Current User")
                .login("currentuser")
                .build();
        UserDTO dto = UserDTO.builder()
                .id("test.com/currentuser")
                .name("Current User")
                .login("currentuser")
                .build();

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(ldapDetails, "password");

        when(userRepo.findById("test.com/currentuser")).thenReturn(Mono.just(appUser));
        when(userMapper.userToDTO(appUser)).thenReturn(dto);

        // Act
        Mono<UserDTO> result = userService.getUser(authToken);

        // Assert
        StepVerifier.create(result)
                .expectNext(dto)
                .verifyComplete();
    }

    // --- saveUser ---

    @Test
    void saveUser_WhenUserExists_ShouldUpdate() {
        // Arrange
        String userId = "existing-user";
        UserDTO inputDto = UserDTO.builder().id(userId).name("Updated Name").login("updatedlogin").build();
        AppUser existingUser = AppUser.builder().id(userId).name("Old Name").login("oldlogin").build();
        AppUser savedUser = AppUser.builder().id(userId).name("Updated Name").login("updatedlogin").build();
        UserDTO savedDto = UserDTO.builder().id(userId).name("Updated Name").login("updatedlogin").build();

        when(userRepo.findById(userId)).thenReturn(Mono.just(existingUser));
        when(userRepo.save(existingUser)).thenReturn(Mono.just(savedUser));
        when(userMapper.userToDTO(savedUser)).thenReturn(savedDto);

        // Act
        Mono<UserDTO> result = userService.saveUser(inputDto);

        // Assert
        StepVerifier.create(result)
                .expectNext(savedDto)
                .verifyComplete();

        assert existingUser.getLogin().equals("updatedlogin");
        assert existingUser.getName().equals("Updated Name");
        verify(userRepo).save(existingUser);
        verify(userMapper).userToDTO(savedUser);
    }

    @Test
    void saveUser_WhenUserDoesNotExist_ShouldCreateNew() {
        // Arrange
        String userId = "new-user";
        UserDTO inputDto = UserDTO.builder().id(userId).name("New User").login("newlogin").build();
        AppUser newUser = AppUser.builder()
                .id(userId)
                .isNew(true)
                .build();
        newUser.setName("New User");
        newUser.setLogin("newlogin");
        AppUser savedUser = AppUser.builder().id(userId).name("New User").login("newlogin").build();
        UserDTO savedDto = UserDTO.builder().id(userId).name("New User").login("newlogin").build();

        when(userRepo.findById(userId)).thenReturn(Mono.empty());
        when(userRepo.save(newUser)).thenReturn(Mono.just(savedUser));
        when(userMapper.userToDTO(savedUser)).thenReturn(savedDto);

        // Act
        Mono<UserDTO> result = userService.saveUser(inputDto);

        // Assert
        StepVerifier.create(result)
                .expectNext(savedDto)
                .verifyComplete();

        verify(userRepo).findById(userId);
        verify(userRepo).save(newUser);
    }

    // --- Stub methods return null ---

    @Test
    void deleteUser_ShouldReturnNull() {
        // Arrange
        UserDTO user = UserDTO.builder().id("1").name("User").login("user").build();

        // Act
        Mono<Void> result = userService.deleteUser(user);

        // Assert
        assert result == null;
    }

    @Test
    void createKeyPare_ShouldReturnNull() {
        // Arrange
        UserDTO user = UserDTO.builder().id("1").name("User").login("user").build();

        // Act
        Mono<Void> result = userService.createKeyPare(user);

        // Assert
        assert result == null;
    }

    @Test
    void getPrivateKey_ShouldReturnNull() {
        // Arrange
        UserDTO user = UserDTO.builder().id("1").name("User").login("user").build();

        // Act
        Mono<String> result = userService.getPrivateKey(user);

        // Assert
        assert result == null;
    }
}
