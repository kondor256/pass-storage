package ru.arbis29.passstorage.domain;

import io.netty.util.internal.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("stored_passwords")
public class StoredPassword implements Persistable<String> {
    @Id
    private String id;
    private String description;
    private String url;
    private String login;
    private String password;
    private byte[] encryptedPassword;

    @Column("owner_user_id")
    private String ownerUserId;

    @Column("folder_id")
    private String folderId;

    @Override
    public boolean isNew() {
        if (StringUtil.isNullOrEmpty(this.id)){
            this.id = UUID.randomUUID().toString();
            return true;
        }
        return false;
    }
}
