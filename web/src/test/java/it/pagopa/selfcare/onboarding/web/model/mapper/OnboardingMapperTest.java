package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingResource;
import it.pagopa.selfcare.onboarding.web.model.InstitutionResource;
import it.pagopa.selfcare.onboarding.web.model.OnboardingResponse;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class OnboardingMapperTest {

    @Test
    void toResource_nullModel() {
        // given
        OnboardingResource model = null;
        // when
        OnboardingResponse resource = OnboardingMapper.toResource(model);
        // then
        assertNull(resource);
    }


    @Test
    void toResource_notNullModel() throws IOException {
        File tempFile = File.createTempFile("hello", ".file");
        try {
            // given
            DummyOnboardingResource model = TestUtils.mockInstance(new DummyOnboardingResource(), "setDocument");
            model.setDocument(tempFile);
            // when
            OnboardingResponse resource = OnboardingMapper.toResource(model);
            // then
            assertNotNull(resource);
            assertEquals(model.getToken(), resource.getToken());
            assertEquals(model.getDocument(), resource.getDocument());
        } finally {
            tempFile.deleteOnExit();
        }
    }

    @Test
    void toResource_institutionInfo() {
        //given
        InstitutionInfo model = TestUtils.mockInstance(new InstitutionInfo());
        //when
        InstitutionResource resource = OnboardingMapper.toResource(model);
        //then
        assertNotNull(resource);
        assertEquals(resource.getId(), model.getInstitutionId());
        assertEquals(resource.getName(), model.getDescription());
    }

    @Test
    void toResource_nullInstitutionInfo() {
        //given
        //when
        InstitutionResource resource = OnboardingMapper.toResource((InstitutionInfo) null);
        //then
        assertNull(resource);
    }

    @Getter
    @Setter
    private static class DummyOnboardingResource implements OnboardingResource {
        private String token;
        private File document;
    }

}