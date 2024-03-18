package ru.arbis29.passstorage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FolderDTO {
    private String id;
    private String name;
    private String folderId;
    private String ownerUserId;
}
