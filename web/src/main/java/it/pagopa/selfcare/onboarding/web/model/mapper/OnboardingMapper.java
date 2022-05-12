package it.pagopa.selfcare.onboarding.web.model.mapper;

import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.*;
import it.pagopa.selfcare.onboarding.connector.model.user.WorkContact;
import it.pagopa.selfcare.onboarding.web.model.*;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class OnboardingMapper {

    public static Billing fromDto(BillingDataDto model) {
        Billing resource = null;
        if (model != null) {
            resource = new Billing();
            resource.setVatNumber(model.getVatNumber());
            resource.setRecipientCode(model.getRecipientCode());
            if (model.getPublicServices() != null) {
                resource.setPublicServices(model.getPublicServices());
            }
        }
        return resource;
    }

    public static InstitutionUpdate mapInstitutionUpdate(BillingDataDto dto) {
        InstitutionUpdate resource = null;
        if (dto != null) {
            resource = new InstitutionUpdate();
            resource.setAddress(dto.getRegisteredOffice());
            resource.setDigitalAddress(dto.getDigitalAddress());
            resource.setDescription(dto.getBusinessName());
            resource.setTaxCode(dto.getTaxCode());
        }
        return resource;
    }

    public static OnboardingData toOnboardingData(String institutionId, String productId, OnboardingDto model) {
        OnboardingData resource = null;
        if (model != null) {
            resource = new OnboardingData();
            resource.setUsers(model.getUsers().stream()
                    .map(OnboardingMapper::toUser)
                    .collect(Collectors.toList()));
            resource.setInstitutionId(institutionId);
            resource.setProductId(productId);
            resource.setOrigin(model.getOrigin());
            resource.setInstitutionUpdate(mapInstitutionUpdate(model.getBillingData()));
            if (model.getBillingData() != null) {
                resource.setBilling(fromDto(model.getBillingData()));
            }
            resource.setInstitutionType(model.getInstitutionType());
        }
        return resource;
    }

    public static User toUser(UserDto model) {
        User resource = null;
        if (model != null) {
            resource = new User();
            resource.setRole(model.getRole());
            resource.setEmail(model.getEmail());
            resource.setName(model.getName());
            resource.setSurname(model.getSurname());
            resource.setProductRole(model.getProductRole());
            resource.setTaxCode(model.getTaxCode());
        }
        return resource;
    }

    public static InstitutionResource toResource(InstitutionInfo model) {
        InstitutionResource resource = null;
        if (model != null) {
            resource = new InstitutionResource();
            if (model.getId() != null) {
                resource.setId(UUID.fromString(model.getId()));
            }
            resource.setDescription(model.getDescription());
            resource.setExternalId(model.getExternalId());
            resource.setAddress(model.getAddress());
            resource.setDigitalAddress(model.getDigitalAddress());
            resource.setTaxCode(model.getTaxCode());
            resource.setZipCode(model.getZipCode());
            resource.setOrigin(model.getOrigin());
        }
        return resource;
    }

    public static InstitutionResource toResource(Institution model) {
        InstitutionResource resource = null;
        if (model != null) {
            resource = new InstitutionResource();
            if (model.getId() != null) {
                resource.setId(UUID.fromString(model.getId()));
            }
            resource.setDescription(model.getDescription());
            resource.setExternalId(model.getExternalId());
            resource.setAddress(model.getAddress());
            resource.setOriginId(model.getOriginId());
            resource.setInstitutionType(model.getInstitutionType());
            resource.setDigitalAddress(model.getDigitalAddress());
            resource.setTaxCode(model.getTaxCode());
            resource.setZipCode(model.getZipCode());
            resource.setOrigin(model.getOrigin());
        }
        return resource;
    }

    public static InstitutionOnboardingInfoResource toResource(InstitutionOnboardingData model) {
        InstitutionOnboardingInfoResource resource = null;
        if (model != null) {
            resource = new InstitutionOnboardingInfoResource();
            resource.setManager(toResource(model.getManager()));
            resource.setInstitution(toData(model.getInstitution()));
        }
        return resource;
    }

    public static InstitutionData toData(InstitutionInfo model) {
        InstitutionData resource = null;
        if (model != null) {
            resource = new InstitutionData();
            BillingDataDto billing = new BillingDataDto();
            billing.setDigitalAddress(model.getDigitalAddress());
            billing.setTaxCode(model.getTaxCode());
            billing.setBusinessName(model.getDescription());
            billing.setRegisteredOffice(model.getAddress());
            if (model.getBilling() != null) {
                billing.setPublicServices(model.getBilling().getPublicServices());
                billing.setRecipientCode(model.getBilling().getRecipientCode());
                billing.setVatNumber(model.getBilling().getVatNumber());
            }
            resource.setBillingData(billing);
            resource.setOrigin(model.getOrigin());
            resource.setInstitutionType(model.getInstitutionType());
        }
        return resource;
    }


    public static UserResource toResource(UserInfo model) {
        UserResource resource = null;
        if (model != null) {
            resource = new UserResource();
            resource.setId(UUID.fromString(model.getId()));
            resource.setRole(model.getRole());
            resource.setStatus(model.getStatus());
            resource.setInstitutionId(UUID.fromString(model.getInstitutionId()));
            if (model.getUser() != null) {
                resource.setName(CertifiedFieldMapper.map(model.getUser().getName()));
                resource.setTaxCode(model.getUser().getFiscalCode());
                resource.setSurname(CertifiedFieldMapper.map(model.getUser().getFamilyName()));
                resource.setEmail(Optional.ofNullable(model.getUser().getWorkContacts())
                        .map(map -> map.get(model.getInstitutionId()))
                        .map(WorkContact::getEmail)
                        .map(CertifiedFieldMapper::map)
                        .orElse(null));
            }
        }
        return resource;
    }

}
