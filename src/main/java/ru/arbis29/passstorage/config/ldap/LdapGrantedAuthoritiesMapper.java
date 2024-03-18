package ru.arbis29.passstorage.config.ldap;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public class LdapGrantedAuthoritiesMapper implements GrantedAuthoritiesMapper {
    private final Set<String> adminRoles;

    public LdapGrantedAuthoritiesMapper(Set<String> adminRoles) {
        this.adminRoles = adminRoles;
    }

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<LdapAuthority> roles = EnumSet.noneOf(LdapAuthority.class);

        for (GrantedAuthority authority : authorities) {
            if (adminRoles.contains(authority.getAuthority())){
                roles.add(LdapAuthority.APP_ADMIN);
            }
        }

        return roles;
    }


}
