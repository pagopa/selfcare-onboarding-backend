package it.pagopa.selfcare.onboarding.web.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

class PartyAuthenticationProviderTest {

    @Test
    void additionalAuthenticationChecks() {
        PartyAuthenticationProvider authenticationProvider = new PartyAuthenticationProvider();
        Assertions.assertAll(() -> authenticationProvider.additionalAuthenticationChecks(null, null));
    }

    @Test
    void retrieveUser() {
        // given
        String username = "username";
        PartyAuthenticationProvider authenticationProvider = new PartyAuthenticationProvider();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, "credentials");
        // when
        UserDetails userDetails = authenticationProvider.retrieveUser(username, authentication);
        // then
        Assertions.assertNotNull(userDetails);
        Assertions.assertNotNull(userDetails.getAuthorities());
        Assertions.assertTrue(userDetails.getAuthorities().isEmpty());
    }

}