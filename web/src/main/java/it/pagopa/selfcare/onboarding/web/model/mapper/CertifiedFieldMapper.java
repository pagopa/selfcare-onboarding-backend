package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.user.Certification;
import it.pagopa.selfcare.onboarding.connector.model.user.CertifiedField;
import it.pagopa.selfcare.onboarding.web.model.CertifiedFieldResource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CertifiedFieldMapper {

    public static <T> CertifiedFieldResource<T> map(CertifiedField<T> certifiedField) {
        CertifiedFieldResource<T> resource = null;
        if (certifiedField != null) {
            resource = new CertifiedFieldResource<>();
            resource.setCertified(Certification.isCertified(certifiedField.getCertification()));
            resource.setValue(certifiedField.getValue());
        }
        return resource;
    }

}
