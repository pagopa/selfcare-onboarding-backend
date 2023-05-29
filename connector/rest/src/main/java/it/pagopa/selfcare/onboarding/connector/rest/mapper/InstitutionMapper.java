package it.pagopa.selfcare.onboarding.connector.rest.mapper;


import it.pagopa.selfcare.onboarding.connector.model.institutions.*;
import it.pagopa.selfcare.onboarding.connector.rest.model.InstitutionResponse;
import it.pagopa.selfcare.onboarding.connector.rest.model.OnboardingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface InstitutionMapper {

    @Mapping(target = "companyInformations", source = ".", qualifiedByName = "toCompanyInformationsEntity")
    @Mapping(target = "assistanceContacts", source = ".", qualifiedByName = "toAssistanceContacts")
    Institution toEntity(InstitutionResponse dto);

    @Named("toCompanyInformationsEntity")
    static CompanyInformations toCompanyInformationsEntity(InstitutionResponse dto) {

        CompanyInformations companyInformations = new CompanyInformations();
        companyInformations.setRea(dto.getRea());
        companyInformations.setShareCapital(dto.getShareCapital());
        companyInformations.setBusinessRegisterPlace(dto.getBusinessRegisterPlace());
        return companyInformations;
    }

    @Named("toAssistanceContacts")
    static AssistanceContacts toAssistanceContacts(InstitutionResponse dto) {

        AssistanceContacts assistanceContacts = new AssistanceContacts();
        assistanceContacts.setSupportEmail(dto.getSupportEmail());
        assistanceContacts.setSupportPhone(dto.getSupportPhone());
        return assistanceContacts;
    }

    OnboardingResource toResource(OnboardingResponse response);
}
