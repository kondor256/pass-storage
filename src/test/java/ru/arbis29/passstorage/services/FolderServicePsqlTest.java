package ru.arbis29.passstorage.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.arbis29.passstorage.domain.PasswordFolder;
import ru.arbis29.passstorage.mappers.FolderMapper;
import ru.arbis29.passstorage.model.FolderDTO;
import ru.arbis29.passstorage.repo.FolderRepo;

import java.security.Principal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolderServicePsqlTest {

    @Mock
    private FolderRepo folderRepo;

    @Mock
    private FolderMapper folderMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private FolderServicePsql folderService;

    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        // Setup mock principal
        mockPrincipal = mock(Principal.class);
    }

    @Test
    void listFolders_ShouldReturnFluxOfFolders() {
        // Arrange
        String userId = "test-user-id";
        when(userService.getUserId(mockPrincipal)).thenReturn(userId);

        PasswordFolder folder1 = new PasswordFolder();
        folder1.setId("1");
        folder1.setName("Folder 1");
        folder1.setOwnerUserId(userId);

        PasswordFolder folder2 = new PasswordFolder();
        folder2.setId("2");
        folder2.setName("Folder 2");
        folder2.setOwnerUserId(userId);

        when(folderRepo.findAllByOwnerUserIdOrderByName(userId))
                .thenReturn(Flux.just(folder1, folder2));

        FolderDTO dto1 = new FolderDTO();
        dto1.setId("1");
        dto1.setName("Folder 1");

        FolderDTO dto2 = new FolderDTO();
        dto2.setId("2");
        dto2.setName("Folder 2");

        when(folderMapper.fldToDTO(folder1)).thenReturn(dto1);
        when(folderMapper.fldToDTO(folder2)).thenReturn(dto2);

        // Act
        Flux<FolderDTO> result = folderService.listFolders(mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .expectNext(dto1, dto2)
                .verifyComplete();

        verify(userService).getUserId(mockPrincipal);
        verify(folderRepo).findAllByOwnerUserIdOrderByName(userId);
        verify(folderMapper).fldToDTO(folder1);
        verify(folderMapper).fldToDTO(folder2);
    }

    @Test
    void listFolders_WhenNoFolders_ShouldReturnEmptyFlux() {
        // Arrange
        String userId = "test-user-id";
        when(userService.getUserId(mockPrincipal)).thenReturn(userId);
        when(folderRepo.findAllByOwnerUserIdOrderByName(userId))
                .thenReturn(Flux.empty());

        // Act
        Flux<FolderDTO> result = folderService.listFolders(mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(userService).getUserId(mockPrincipal);
        verify(folderRepo).findAllByOwnerUserIdOrderByName(userId);
    }

    @Test
    void getFolderById_WhenFolderExists_ShouldReturnFolderDto() {
        // Arrange
        String userId = "test-user-id";
        String folderId = "folder-123";
        when(userService.getUserId(mockPrincipal)).thenReturn(userId);

        PasswordFolder folder = new PasswordFolder();
        folder.setId(folderId);
        folder.setName("Test Folder");
        folder.setOwnerUserId(userId);

        when(folderRepo.findFirstByIdAndOwnerUserId(folderId, userId))
                .thenReturn(Mono.just(folder));

        FolderDTO expectedDto = new FolderDTO();
        expectedDto.setId(folderId);
        expectedDto.setName("Test Folder");

        when(folderMapper.fldToDTO(folder)).thenReturn(expectedDto);

        // Act
        Mono<FolderDTO> result = folderService.getFolderById(folderId, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(dto ->
                        dto.getId().equals(folderId) &&
                                dto.getName().equals("Test Folder"))
                .verifyComplete();

        verify(userService).getUserId(mockPrincipal);
        verify(folderRepo).findFirstByIdAndOwnerUserId(folderId, userId);
        verify(folderMapper).fldToDTO(folder);
    }

    @Test
    void getFolderById_WhenFolderDoesNotExist_ShouldReturnEmptyMono() {
        // Arrange
        String userId = "test-user-id";
        String folderId = "non-existent-id";
        when(userService.getUserId(mockPrincipal)).thenReturn(userId);
        when(folderRepo.findFirstByIdAndOwnerUserId(folderId, userId))
                .thenReturn(Mono.empty());

        // Act
        Mono<FolderDTO> result = folderService.getFolderById(folderId, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(userService).getUserId(mockPrincipal);
        verify(folderRepo).findFirstByIdAndOwnerUserId(folderId, userId);
    }

    @Test
    void saveNewFolder_WhenFolderHasNoId_ShouldSaveNewFolder() {
        // Arrange
        String userId = "test-user-id";
        when(userService.getUserId(mockPrincipal)).thenReturn(userId);

        FolderDTO inputDto = new FolderDTO();
        inputDto.setName("New Folder");

        PasswordFolder folderToSave = new PasswordFolder();
        folderToSave.setName("New Folder");
        folderToSave.setOwnerUserId(userId);

        PasswordFolder savedFolder = new PasswordFolder();
        savedFolder.setId("generated-id");
        savedFolder.setName("New Folder");
        savedFolder.setOwnerUserId(userId);

        FolderDTO resultDto = new FolderDTO();
        resultDto.setId("generated-id");
        resultDto.setName("New Folder");

        when(folderMapper.DTOToFld(inputDto)).thenReturn(folderToSave);
        when(folderRepo.save(folderToSave)).thenReturn(Mono.just(savedFolder));
        when(folderMapper.fldToDTO(savedFolder)).thenReturn(resultDto);

        // Act
        Mono<FolderDTO> result = folderService.saveNewFolder(inputDto, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(dto ->
                        dto.getId().equals("generated-id") &&
                                dto.getName().equals("New Folder"))
                .verifyComplete();

        verify(userService).getUserId(mockPrincipal);
        verify(folderMapper).DTOToFld(inputDto);
        verify(folderRepo).save(folderToSave);
        verify(folderMapper).fldToDTO(savedFolder);
        // Verify ownerUserId was set
        assert folderToSave.getOwnerUserId().equals(userId);
    }

    @Test
    void saveNewFolder_WhenFolderHasIdAndExists_ShouldUpdateFolder() {
        // Arrange
        String userId = "test-user-id";
        String folderId = "existing-id";
        when(userService.getUserId(mockPrincipal)).thenReturn(userId);

        FolderDTO inputDto = new FolderDTO();
        inputDto.setId(folderId);
        inputDto.setName("Updated Folder");

        PasswordFolder folderToSave = new PasswordFolder();
        folderToSave.setId(folderId);
        folderToSave.setName("Updated Folder");
        folderToSave.setOwnerUserId(userId);

        PasswordFolder existingFolder = new PasswordFolder();
        existingFolder.setId(folderId);
        existingFolder.setName("Original Folder");
        existingFolder.setOwnerUserId(userId);

        FolderDTO resultDto = new FolderDTO();
        resultDto.setId(folderId);
        resultDto.setName("Updated Folder");

        when(folderMapper.DTOToFld(inputDto)).thenReturn(folderToSave);
        when(folderRepo.findFirstByIdAndOwnerUserId(folderId, userId))
                .thenReturn(Mono.just(existingFolder));
        when(folderRepo.save(folderToSave)).thenReturn(Mono.just(folderToSave));
        when(folderMapper.fldToDTO(folderToSave)).thenReturn(resultDto);

        // Act
        Mono<FolderDTO> result = folderService.saveNewFolder(inputDto, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(dto ->
                        dto.getId().equals(folderId) &&
                                dto.getName().equals("Updated Folder"))
                .verifyComplete();

        verify(userService).getUserId(mockPrincipal);
        verify(folderMapper).DTOToFld(inputDto);
        verify(folderRepo).findFirstByIdAndOwnerUserId(folderId, userId);
        verify(folderRepo).save(folderToSave);
        verify(folderMapper).fldToDTO(folderToSave);
    }

    @Test
    void saveNewFolder_WhenFolderHasIdButDoesNotExist_ShouldSaveAsNew() {
        // Arrange
        String userId = "test-user-id";
        String folderId = "non-existent-id";
        when(userService.getUserId(mockPrincipal)).thenReturn(userId);

        FolderDTO inputDto = new FolderDTO();
        inputDto.setId(folderId);
        inputDto.setName("New Folder");

        PasswordFolder folderToSave = new PasswordFolder();
        folderToSave.setId(folderId);
        folderToSave.setName("New Folder");
        folderToSave.setOwnerUserId(userId);

        PasswordFolder savedFolder = new PasswordFolder();
        savedFolder.setId(folderId);
        savedFolder.setName("New Folder");
        savedFolder.setOwnerUserId(userId);

        FolderDTO resultDto = new FolderDTO();
        resultDto.setId(folderId);
        resultDto.setName("New Folder");

        when(folderMapper.DTOToFld(inputDto)).thenReturn(folderToSave);
        when(folderRepo.findFirstByIdAndOwnerUserId(folderId, userId))
                .thenReturn(Mono.empty());
        when(folderRepo.save(folderToSave)).thenReturn(Mono.just(savedFolder));
        when(folderMapper.fldToDTO(savedFolder)).thenReturn(resultDto);

        // Act
        Mono<FolderDTO> result = folderService.saveNewFolder(inputDto, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(dto ->
                        dto.getId().equals(folderId) &&
                                dto.getName().equals("New Folder"))
                .verifyComplete();

        verify(userService).getUserId(mockPrincipal);
        verify(folderMapper).DTOToFld(inputDto);
        verify(folderRepo).findFirstByIdAndOwnerUserId(folderId, userId);
        verify(folderRepo).save(folderToSave);
        verify(folderMapper).fldToDTO(savedFolder);
    }

    @Test
    void deleteFolderById_ShouldDeleteFolder() {
        // Arrange
        String userId = "test-user-id";
        String folderId = "folder-to-delete";
        when(userService.getUserId(mockPrincipal)).thenReturn(userId);
        when(folderRepo.deleteByIdAndOwnerUserId(folderId, userId))
                .thenReturn(Mono.empty());

        // Act
        Mono<Void> result = folderService.deleteFolderById(folderId, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(userService).getUserId(mockPrincipal);
        verify(folderRepo).deleteByIdAndOwnerUserId(folderId, userId);
    }

    @Test
    void deleteFolderById_WhenFolderDoesNotExist_ShouldCompleteWithoutError() {
        // Arrange
        String userId = "test-user-id";
        String folderId = "non-existent-id";
        when(userService.getUserId(mockPrincipal)).thenReturn(userId);
        when(folderRepo.deleteByIdAndOwnerUserId(folderId, userId))
                .thenReturn(Mono.empty());

        // Act
        Mono<Void> result = folderService.deleteFolderById(folderId, mockPrincipal);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(userService).getUserId(mockPrincipal);
        verify(folderRepo).deleteByIdAndOwnerUserId(folderId, userId);
    }
}