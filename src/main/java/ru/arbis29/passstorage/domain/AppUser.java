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
@Table("app_users")
public class AppUser implements Persistable<String> {
    @Id
    private String id;
    private String name;
    private String login;

//    private Byte[] privKey;
//    private Byte[] pubKey;
    @Transient
    private Boolean isNew;

    @Override
    public boolean isNew() {
        if (StringUtil.isNullOrEmpty(this.id)){
            this.id = UUID.randomUUID().toString();
            return true;
        }
        if (isNew == null) return false;
        return isNew;
    }
}
