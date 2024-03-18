package ru.arbis29.passstorage.domain;

import io.netty.util.internal.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("users_keys")
public class UsersKeys implements Persistable<String> {
    @Id
    private String userId;
    private byte[] privKey;
    private byte[] pubKey;

    @Transient
    private Boolean isNew;

    @Override
    public String getId() {
        return userId;
    }

    @Override
    public boolean isNew() {
        if (StringUtil.isNullOrEmpty(this.userId)){
            this.userId = UUID.randomUUID().toString();
            return true;
        }
        if (isNew == null) return false;
        return isNew;
    }
}
