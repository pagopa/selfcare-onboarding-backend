package it.pagopa.selfcare.onboarding.web.config;

import it.pagopa.selfcare.commons.web.config.SecurityConfig;
import it.pagopa.selfcare.commons.web.security.JwtService;
import it.pagopa.selfcare.onboarding.web.security.PartyAuthenticationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.ADMIN;
import static it.pagopa.selfcare.commons.base.security.SelfCareAuthority.LIMITED;

@Slf4j
@Configuration
@EnableWebSecurity
class OnboardingSecurityConfig extends SecurityConfig {

    private final PartyAuthenticationProvider authenticationProvider;


    @Autowired
    public OnboardingSecurityConfig(JwtService jwtService, PartyAuthenticationProvider authenticationProvider) {
        super(jwtService);
        this.authenticationProvider = authenticationProvider;
    }


    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(ADMIN.name() + " > " + LIMITED.name());
        RoleHierarchyAuthoritiesMapper authoritiesMapper = new RoleHierarchyAuthoritiesMapper(roleHierarchy);
        authenticationProvider.setAuthoritiesMapper(authoritiesMapper);
        authenticationManagerBuilder.authenticationProvider(authenticationProvider);
        authenticationManagerBuilder.eraseCredentials(false);
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/institutions/**").fullyAuthenticated()
                .anyRequest().permitAll();
        super.configure(http);
    }

}