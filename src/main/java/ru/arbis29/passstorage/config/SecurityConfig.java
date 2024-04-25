package ru.arbis29.passstorage.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.security.authentication.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.server.SecurityWebFilterChain;
import ru.arbis29.passstorage.config.ldap.LdapGrantedAuthoritiesMapper;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebFluxSecurity
@ConfigurationProperties(prefix = "ldap")
@RequiredArgsConstructor
public class SecurityConfig {
    @Getter @Setter
    private String url;
    @Getter @Setter
    private String domain;
    @Getter @Setter
    private String base;
    @Getter @Setter
    private Set<String> adminRoles;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/favicon.ico").permitAll()
                        .pathMatchers("/built/**").permitAll()
                        .anyExchange().hasRole("APP_ADMIN")//.authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin((formLoginConfigurer)->{})
                .csrf(ServerHttpSecurity.CsrfSpec::disable);

        return http.build();
    }

    @Bean
    public GrantedAuthoritiesMapper grantedAuthoritiesMapper(){
        return new LdapGrantedAuthoritiesMapper(adminRoles);
    }

    @Bean
    ActiveDirectoryLdapAuthenticationProvider authenticationProvider() {
        ActiveDirectoryLdapAuthenticationProvider prov = new ActiveDirectoryLdapAuthenticationProvider(domain, url);
        prov.setAuthoritiesMapper(grantedAuthoritiesMapper());

        return prov;
    }

    @Bean
    ReactiveAuthenticationManager authenticationManager(AuthenticationEventPublisher publisher) {

        ActiveDirectoryLdapAuthenticationProvider ldap = new ActiveDirectoryLdapAuthenticationProvider(domain, url);
        ldap.setAuthoritiesMapper(grantedAuthoritiesMapper());

        ProviderManager pm = new ProviderManager(List.of(ldap));
        pm.setAuthenticationEventPublisher(publisher);
        //AuthenticationManager am = ;
        return new ReactiveAuthenticationManagerAdapter(pm);

    }
    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(url);
        contextSource.setBase(base);

//        contextSource.setAnonymousReadOnly(true);
//        contextSource.setUserDn("_kondor@ARBIS-NEW.local");
//        contextSource.setPassword("CNJgbljhfcjd123");

        return contextSource;
    }
    @Bean
    public LdapTemplate ldapTemplate(ContextSource contextSource) {
        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);

//        List<Attributes> distinguishedNames = ldapTemplate.search(
//                LdapQueryBuilder.query().where("objectCategory").is("user"),
//                //LdapQueryBuilder.query().where("name").isPresent(),
//                (AttributesMapper<Attributes>) attrs -> attrs//(String) attrs.get("cn").get()
//        );
//        for (Attributes user : distinguishedNames){
//            byte[] userCERT;
//            try {
//                Attribute attribute = user.get("userCertificate");
//                if (attribute != null) {
//                    userCERT = (byte[])attribute.get();
//
//                    ByteArrayInputStream bais = new ByteArrayInputStream((userCERT));
//                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
//                    X509Certificate cert = (X509Certificate) cf.generateCertificate(bais);
//                    cert.getPublicKey();
//                }
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }

        return ldapTemplate;
    }

    @Bean
    public AuthenticationEventPublisher authenticationEventPublisher
            (ApplicationEventPublisher applicationEventPublisher) {
        return new DefaultAuthenticationEventPublisher(applicationEventPublisher);
    }
}
