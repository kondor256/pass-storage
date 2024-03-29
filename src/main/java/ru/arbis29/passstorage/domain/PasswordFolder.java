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

import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("password_folders")
public class PasswordFolder implements Persistable<String> {
    @Id
    private String id;
    private String name;
    @Column("folder_id")
    private String folderId;
    @Column("owner_user_id")
    private String ownerUserId;


    @Override
    public boolean isNew() {
        if (StringUtil.isNullOrEmpty(this.id)){
            this.id = UUID.randomUUID().toString();
            return true;
        }
        return false;
    }
}
