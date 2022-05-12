package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.user.Certification;
import it.pagopa.selfcare.onboarding.connector.model.user.CertifiedField;
import it.pagopa.selfcare.onboarding.connector.model.user.WorkContact;
import it.pagopa.selfcare.onboarding.web.model.CertifiedFieldResource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CertifiedFieldMapper {

    public static String toValue(CertifiedField<String> certifiedField) {
        return certifiedField != null ? certifiedField.getValue() : null;
    }


    public static <T> CertifiedFieldResource<T> map(CertifiedField<T> certifiedField) {
        CertifiedFieldResource<T> resource = null;
        if (certifiedField != null) {
            resource = new CertifiedFieldResource<>();
            resource.setCertified(Certification.isCertified(certifiedField.getCertification()));
            resource.setValue(certifiedField.getValue());
        }
        return resource;
    }


    public static CertifiedFieldResource<String> getWorkEmail(Map<String, WorkContact> workContacts, String institutionId) {
        CertifiedFieldResource<String> resource = null;
        if (workContacts != null) {
            final WorkContact workContact = workContacts.get(institutionId);
            if (workContact != null) {
                resource = map(workContact.getEmail());
            }
        }
        return resource;
    }

}
