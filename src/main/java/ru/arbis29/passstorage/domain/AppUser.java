package ru.arbis29.passstorage.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("app_users")
public class AppUser {
    @Id
    private String id;
    private String name;
}
