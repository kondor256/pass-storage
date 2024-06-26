package ru.arbis29.passstorage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordDTO {
    private String id;
    private String name;
    private String description;
    private String url;
    private String login;
    private String password;
    private byte[] encryptedPassword;

    private String folderId;
    private String ownerUserId;

    private boolean shared;
}
