package ru.arbis29.passstorage.config.ldap;

import org.springframework.security.core.GrantedAuthority;

public enum LdapAuthority implements GrantedAuthority {
    APP_USER, APP_ADMIN;

    @Override
    public String getAuthority() {
        return "ROLE_"+name();
    }
}
