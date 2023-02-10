package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.connector.model.user.Certification;
import it.pagopa.selfcare.onboarding.connector.model.user.CertifiedField;
import it.pagopa.selfcare.onboarding.core.exception.InvalidUserFieldsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumSet;
import java.util.Optional;

import static it.pagopa.selfcare.onboarding.connector.model.user.User.Fields.familyName;
import static it.pagopa.selfcare.onboarding.connector.model.user.User.Fields.name;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {


    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRegistryConnector userRegistryConnectorMock;


    @Test
    void validate_nullUser() {
        // given
        User user = null;
        // when
        final Executable executable = () -> userService.validate(user);
        // then
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An user is required", e.getMessage());
        verifyNoInteractions(userRegistryConnectorMock);
    }


    @Test
    void validate_userNotFound() {
        // given
        User user = TestUtils.mockInstance(new User());
        // when
        userService.validate(user);
        // then
        verify(userRegistryConnectorMock, times(1))
                .search(user.getTaxCode(), EnumSet.of(name, familyName));
        verifyNoMoreInteractions(userRegistryConnectorMock);
    }


    @Test
    void validate_validUserData() {
        // given
        User user = TestUtils.mockInstance(new User());
        final it.pagopa.selfcare.onboarding.connector.model.user.User userFound =
                new it.pagopa.selfcare.onboarding.connector.model.user.User();
        final CertifiedField<String> certifiedFamilyName = new CertifiedField<>();
        certifiedFamilyName.setCertification(Certification.NONE);
        userFound.setFamilyName(certifiedFamilyName);
        when(userRegistryConnectorMock.search(any(), any()))
                .thenReturn(Optional.of(userFound));
        // when
        userService.validate(user);
        // then
        verify(userRegistryConnectorMock, times(1))
                .search(user.getTaxCode(), EnumSet.of(name, familyName));
        verifyNoMoreInteractions(userRegistryConnectorMock);
    }


    @Test
    void validate_invalidUserData() {
        // given
        User user = TestUtils.mockInstance(new User());
        final it.pagopa.selfcare.onboarding.connector.model.user.User userFound =
                new it.pagopa.selfcare.onboarding.connector.model.user.User();
        final CertifiedField<String> certifiedFamilyName = new CertifiedField<>();
        certifiedFamilyName.setCertification(Certification.SPID);
        certifiedFamilyName.setValue(user.getSurname());
        userFound.setFamilyName(certifiedFamilyName);
        final CertifiedField<String> certifiedName = new CertifiedField<>();
        certifiedName.setCertification(Certification.SPID);
        certifiedName.setValue("different value");
        userFound.setName(certifiedName);
        when(userRegistryConnectorMock.search(any(), any()))
                .thenReturn(Optional.of(userFound));
        // when
        final Executable executable = () -> userService.validate(user);
        // then
        final InvalidUserFieldsException e = assertThrows(InvalidUserFieldsException.class, executable);
        assertNotNull(e.getInvalidFields());
        assertEquals(1, e.getInvalidFields().size());
        assertEquals("name", e.getInvalidFields().get(0).getName());
        assertEquals("the value does not match with the certified data", e.getInvalidFields().get(0).getReason());
        verify(userRegistryConnectorMock, times(1))
                .search(user.getTaxCode(), EnumSet.of(name, familyName));
        verifyNoMoreInteractions(userRegistryConnectorMock);
    }

    @Test
    void validate_invalidUserData_surname() {
        // given
        User user = TestUtils.mockInstance(new User());
        final it.pagopa.selfcare.onboarding.connector.model.user.User userFound =
                new it.pagopa.selfcare.onboarding.connector.model.user.User();
        final CertifiedField<String> certifiedName = new CertifiedField<>();
        certifiedName.setCertification(Certification.SPID);
        certifiedName.setValue(user.getName());
        userFound.setName(certifiedName);
        final CertifiedField<String> certifiedFamilyName = new CertifiedField<>();
        certifiedFamilyName.setCertification(Certification.SPID);
        certifiedFamilyName.setValue("different value");
        userFound.setFamilyName(certifiedFamilyName);
        when(userRegistryConnectorMock.search(any(), any()))
                .thenReturn(Optional.of(userFound));
        // when
        final Executable executable = () -> userService.validate(user);
        // then
        final InvalidUserFieldsException e = assertThrows(InvalidUserFieldsException.class, executable);
        assertNotNull(e.getInvalidFields());
        assertEquals(1, e.getInvalidFields().size());
        assertEquals("surname", e.getInvalidFields().get(0).getName());
        assertEquals("the value does not match with the certified data", e.getInvalidFields().get(0).getReason());
        verify(userRegistryConnectorMock, times(1))
                .search(user.getTaxCode(), EnumSet.of(name, familyName));
        verifyNoMoreInteractions(userRegistryConnectorMock);
    }

}