package ru.arbis29.passstorage.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("stored_passwords")
public class StoredPassword {
    @Id
    private String id;
    private String description;
    private String url;
    private String login;
    private String password;

    @Column("owner_user_id")
    private String ownerUserId;

    @Column("folder_id")
    private String folderId;
}
