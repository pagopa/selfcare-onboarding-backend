package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import it.pagopa.selfcare.commons.base.utils.Origin;
import it.pagopa.selfcare.onboarding.connector.api.*;
import it.pagopa.selfcare.onboarding.connector.exceptions.ResourceNotFoundException;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionLegalAddressData;
import it.pagopa.selfcare.onboarding.connector.model.InstitutionOnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.institutions.Institution;
import it.pagopa.selfcare.onboarding.connector.model.institutions.InstitutionInfo;
import it.pagopa.selfcare.onboarding.connector.model.institutions.MatchInfoResult;
import it.pagopa.selfcare.onboarding.connector.model.institutions.OnboardingResource;
import it.pagopa.selfcare.onboarding.connector.model.institutions.infocamere.InstitutionInfoIC;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.GeographicTaxonomy;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.InstitutionLocation;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.entity.ProductRoleInfo;
import it.pagopa.selfcare.product.entity.ProductStatus;
import it.pagopa.selfcare.onboarding.connector.model.registry_proxy.GeographicTaxonomies;
import it.pagopa.selfcare.onboarding.connector.model.registry_proxy.HomogeneousOrganizationalArea;
import it.pagopa.selfcare.onboarding.connector.model.registry_proxy.InstitutionProxyInfo;
import it.pagopa.selfcare.onboarding.connector.model.registry_proxy.OrganizationUnit;
import it.pagopa.selfcare.onboarding.connector.model.user.Certification;
import it.pagopa.selfcare.onboarding.connector.model.user.CertifiedField;
import it.pagopa.selfcare.onboarding.connector.model.user.MutableUserFieldsDto;
import it.pagopa.selfcare.onboarding.connector.model.user.WorkContact;
import it.pagopa.selfcare.onboarding.connector.model.user.mapper.CertifiedFieldMapper;
import it.pagopa.selfcare.onboarding.connector.model.user.mapper.UserMapper;
import it.pagopa.selfcare.onboarding.core.exception.OnboardingNotAllowedException;
import it.pagopa.selfcare.onboarding.core.exception.UpdateNotAllowedException;
import it.pagopa.selfcare.onboarding.core.mapper.InstitutionInfoMapper;
import it.pagopa.selfcare.onboarding.core.strategy.OnboardingValidationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.validation.ValidationException;
import java.util.*;

import static it.pagopa.selfcare.onboarding.connector.model.user.User.Fields.*;

@Slf4j
@Service
class InstitutionServiceImpl implements InstitutionService {

    protected static final String REQUIRED_INSTITUTION_ID_MESSAGE = "An Institution id is required";
    protected static final String REQUIRED_TAX_CODE_MESSAGE = "A taxCode id is required";
    protected static final String REQUIRED_INSTITUTION_BILLING_DATA_MESSAGE = "Institution's billing data are required";
    protected static final String REQUIRED_INSTITUTION_TYPE_MESSAGE = "An institution type is required";
    protected static final String REQUIRED_INSTITUTION_UPDATE_MESSAGE = "InsitutionUpdate is required";
    protected static final String REQUIRED_ONBOARDING_DATA_MESSAGE = "Onboarding data is required";
    protected static final String ATLEAST_ONE_PRODUCT_ROLE_REQUIRED = "At least one Product role related to %s Party role is required";
    protected static final String MORE_THAN_ONE_PRODUCT_ROLE_AVAILABLE = "More than one Product role related to %s Party role is available. Cannot automatically set the Product role";
    protected static final String A_PRODUCT_ID_IS_REQUIRED = "A Product Id is required";
    protected static final String LOCATION_INFO_IS_REQUIRED = "Location infos are required";
    private static final EnumSet<it.pagopa.selfcare.onboarding.connector.model.user.User.Fields> USER_FIELD_LIST = EnumSet.of(name, familyName, workContacts);
    private static final String ONBOARDING_NOT_ALLOWED_ERROR_MESSAGE_TEMPLATE = "Institution with external id '%s' is not allowed to onboard '%s' product";
    public static final String UNABLE_TO_COMPLETE_THE_ONBOARDING_FOR_INSTITUTION_FOR_PRODUCT_DISMISSED = "Unable to complete the onboarding for institution with taxCode '%s' to product '%s', the product is dismissed.";
    public static final String FIELD_PSP_DATA_IS_REQUIRED_FOR_PSP_INSTITUTION_ONBOARDING = "Field 'pspData' is required for PSP institution onboarding";
    static final String DESCRIPTION_TO_REPLACE_REGEX = " - COMUNE";
    private final OnboardingMsConnector onboardingMsConnector;
    private final PartyConnector partyConnector;
    private final ProductsConnector productsConnector;
    private final UserRegistryConnector userConnector;
    private final MsExternalInterceptorConnector externalInterceptorConnector;
    private final OnboardingValidationStrategy onboardingValidationStrategy;
    private final PartyRegistryProxyConnector partyRegistryProxyConnector;
    private final InstitutionInfoMapper institutionMapper;

    @Autowired
    InstitutionServiceImpl(OnboardingMsConnector onboardingMsConnector, PartyConnector partyConnector,
                           ProductsConnector productsConnector,
                           UserRegistryConnector userConnector,
                           MsExternalInterceptorConnector externalInterceptorConnector,
                           PartyRegistryProxyConnector partyRegistryProxyConnector,
                           OnboardingValidationStrategy onboardingValidationStrategy,
                           InstitutionInfoMapper institutionMapper
                           ) {
        this.onboardingMsConnector = onboardingMsConnector;
        this.partyConnector = partyConnector;
        this.externalInterceptorConnector = externalInterceptorConnector;
        this.partyRegistryProxyConnector = partyRegistryProxyConnector;
        this.productsConnector = productsConnector;
        this.userConnector = userConnector;
        this.onboardingValidationStrategy = onboardingValidationStrategy;
        this.institutionMapper = institutionMapper;
    }


    @Override
    public void onboardingProductV2(OnboardingData onboardingData) {
        log.trace("onboardingProductAsync start");
        log.debug("onboardingProductAsync onboardingData = {}", onboardingData);
        onboardingMsConnector.onboarding(onboardingData);
        log.trace("onboarding end");
    }
    @Override
    public void onboardingCompanyV2(OnboardingData onboardingData) {
        log.trace("onboardingProductAsync start");
        log.debug("onboardingProductAsync onboardingData = {}", onboardingData);
        onboardingMsConnector.onboardingCompany(onboardingData);
        log.trace("onboarding end");
    }

    @Override
    public void onboardingProduct(OnboardingData onboardingData) {
        log.trace("onboarding start");
        log.debug("onboarding onboardingData = {}", onboardingData);

        Assert.notNull(onboardingData, REQUIRED_ONBOARDING_DATA_MESSAGE);
        Assert.notNull(onboardingData.getBilling(), REQUIRED_INSTITUTION_BILLING_DATA_MESSAGE);
        Assert.notNull(onboardingData.getInstitutionType(), REQUIRED_INSTITUTION_TYPE_MESSAGE);
        Assert.notNull(onboardingData.getInstitutionUpdate(), REQUIRED_INSTITUTION_UPDATE_MESSAGE);

        if (InstitutionType.PSP.equals(onboardingData.getInstitutionType()) && onboardingData.getInstitutionUpdate().getPaymentServiceProvider() == null) {
            throw new ValidationException(FIELD_PSP_DATA_IS_REQUIRED_FOR_PSP_INSTITUTION_ONBOARDING);
        }
        if (isLocationInfoRequired(onboardingData.getOrigin()) && onboardingData.getLocation() == null){
            throw new ValidationException(LOCATION_INFO_IS_REQUIRED);
        }

        Product product = productsConnector.getProduct(onboardingData.getProductId(), onboardingData.getInstitutionType());
        Assert.notNull(product, "Product is required");
        checkIfProductIsDelegable(onboardingData, product.isDelegable());
        if(product.getStatus() == ProductStatus.PHASE_OUT){
            throw new ValidationException(String.format(UNABLE_TO_COMPLETE_THE_ONBOARDING_FOR_INSTITUTION_FOR_PRODUCT_DISMISSED,
                    onboardingData.getTaxCode(),
                    product.getId()));
        }

        onboardingData.setContractPath(product.getContractTemplatePath());
        onboardingData.setContractVersion(product.getContractTemplateVersion());

        checkIfProductIsActiveAndSetUserProductRole(product, onboardingData);
        onboardingData.setProductName(product.getTitle());

        Institution institution;
        try {
            institution = partyConnector.getInstitutionsByTaxCodeAndSubunitCode(onboardingData.getTaxCode(), onboardingData.getSubunitCode())
                    .stream()
                    .findFirst()
                    .orElseThrow(ResourceNotFoundException::new);
        } catch (ResourceNotFoundException e) {
            if (InstitutionType.SA.equals(onboardingData.getInstitutionType()) && onboardingData.getOrigin().equalsIgnoreCase(Origin.ANAC.getValue())) {
                institution = partyConnector.createInstitutionFromANAC(onboardingData);
            }
            else if (InstitutionType.AS.equals(onboardingData.getInstitutionType()) && onboardingData.getOrigin().equalsIgnoreCase("IVASS")) {
                institution = partyConnector.createInstitutionFromIVASS(onboardingData);
            }
            else if (InstitutionType.PG.equals(onboardingData.getInstitutionType()) &&
                    (onboardingData.getOrigin().equalsIgnoreCase(Origin.INFOCAMERE.getValue()) || onboardingData.getOrigin().equalsIgnoreCase(Origin.ADE.getValue()))) {
                institution = partyConnector.createInstitutionFromInfocamere(onboardingData);
            }
            else if (isInstitutionPresentOnIpa(onboardingData)) {
                institution = partyConnector.createInstitutionFromIpa(onboardingData.getTaxCode(), onboardingData.getSubunitCode(), onboardingData.getSubunitType());
            } else {
                institution = partyConnector.createInstitution(onboardingData);
            }
        }
        String finalInstitutionInternalId = institution.getId();
        onboardingData.getUsers().forEach(user -> {

            final Optional<it.pagopa.selfcare.onboarding.connector.model.user.User> searchResult =
                    userConnector.search(user.getTaxCode(), USER_FIELD_LIST);
            searchResult.ifPresentOrElse(foundUser -> {
                Optional<MutableUserFieldsDto> updateRequest = createUpdateRequest(user, foundUser, finalInstitutionInternalId);
                updateRequest.ifPresent(mutableUserFieldsDto ->
                        userConnector.updateUser(UUID.fromString(foundUser.getId()), mutableUserFieldsDto));
                user.setId(foundUser.getId());
            }, () -> user.setId(userConnector.saveUser(UserMapper.toSaveUserDto(user, finalInstitutionInternalId))
                    .getId().toString()));
        });

        onboardingData.setInstitutionExternalId(institution.getExternalId());

        partyConnector.onboardingOrganization(onboardingData);
        log.trace("onboarding end");
    }

    private boolean isLocationInfoRequired(String origin) {
        return !Origin.IPA.equals(Origin.fromValue(origin)) &&
                !Origin.ADE.equals(Origin.fromValue(origin)) &&
                !Origin.INFOCAMERE.equals(Origin.fromValue(origin));
    }

    private boolean isInstitutionPresentOnIpa(OnboardingData onboardingData) {
        try {
            if (onboardingData.getSubunitType() != null && onboardingData.getSubunitType().equals("AOO")) {
                partyRegistryProxyConnector.getAooById(onboardingData.getSubunitCode());
            } else if (onboardingData.getSubunitType() != null && onboardingData.getSubunitType().equals("UO")) {
                partyRegistryProxyConnector.getUoById(onboardingData.getSubunitCode());
            } else {
                partyRegistryProxyConnector.getInstitutionProxyById(onboardingData.getTaxCode());
            }
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    private void checkIfProductIsActiveAndSetUserProductRole(Product product, OnboardingData onboardingData) {
        Map<PartyRole, ProductRoleInfo> roleMappings;
        if (product.getParentId() != null) {
            final Product baseProduct = productsConnector.getProduct(product.getParentId(), null);
            if(baseProduct.getStatus() == ProductStatus.PHASE_OUT){
                throw new ValidationException(String.format("Unable to complete the onboarding for institution with taxCode '%s' to product '%s', the base product is dismissed.",
                        onboardingData.getTaxCode(),
                        baseProduct.getId()));
            }
            validateOnboarding(onboardingData.getTaxCode(), baseProduct.getId());
            try {
                partyConnector.verifyOnboarding(baseProduct.getId(), null, onboardingData.getTaxCode(), onboardingData.getOrigin(), null, onboardingData.getSubunitCode());
            } catch (RuntimeException e) {
                throw new ValidationException(String.format("Unable to complete the onboarding for institution with taxCode '%s' to product '%s'. Please onboard first the '%s' product for the same institution",
                        onboardingData.getTaxCode(),
                        product.getId(),
                        baseProduct.getId()));
            }
            roleMappings = baseProduct.getRoleMappings();
        } else {
            validateOnboarding(onboardingData.getTaxCode(), product.getId());
            roleMappings = product.getRoleMappings();
        }

        validateProductRole(onboardingData.getUsers(), roleMappings);
    }

    private void validateProductRole(List<User> users, Map<PartyRole, ProductRoleInfo> roleMappings) {
        Assert.notNull(roleMappings, "Role mappings is required");
        users.forEach(userInfo -> {
            Assert.notNull(roleMappings.get(userInfo.getRole()),
                    String.format(ATLEAST_ONE_PRODUCT_ROLE_REQUIRED, userInfo.getRole()));
            Assert.notEmpty(roleMappings.get(userInfo.getRole()).getRoles(),
                    String.format(ATLEAST_ONE_PRODUCT_ROLE_REQUIRED, userInfo.getRole()));
            Assert.state(roleMappings.get(userInfo.getRole()).getRoles().size() == 1,
                    String.format(MORE_THAN_ONE_PRODUCT_ROLE_AVAILABLE, userInfo.getRole()));
            userInfo.setProductRole(roleMappings.get(userInfo.getRole()).getRoles().get(0).getCode());
        });
    }

    private void checkIfProductIsDelegable(OnboardingData onboardingData, boolean delegable) {
        if(InstitutionType.PT == onboardingData.getInstitutionType() && !delegable) {
            throw new OnboardingNotAllowedException(String.format(ONBOARDING_NOT_ALLOWED_ERROR_MESSAGE_TEMPLATE,
                    onboardingData.getTaxCode(),
                    onboardingData.getProductId()));
        }
    }

    protected static Optional<MutableUserFieldsDto> createUpdateRequest(User user, it.pagopa.selfcare.onboarding.connector.model.user.User foundUser, String institutionInternalId) {
        Optional<MutableUserFieldsDto> mutableUserFieldsDto = Optional.empty();
        if (isFieldToUpdate(foundUser.getName(), user.getName())) {
            MutableUserFieldsDto dto = new MutableUserFieldsDto();
            dto.setName(CertifiedFieldMapper.map(user.getName()));
            mutableUserFieldsDto = Optional.of(dto);
        }
        if (isFieldToUpdate(foundUser.getFamilyName(), user.getSurname())) {
            MutableUserFieldsDto dto = mutableUserFieldsDto.orElseGet(MutableUserFieldsDto::new);
            dto.setFamilyName(CertifiedFieldMapper.map(user.getSurname()));
            mutableUserFieldsDto = Optional.of(dto);
        }
        if (foundUser.getWorkContacts() == null
                || !foundUser.getWorkContacts().containsKey(institutionInternalId)
                || isFieldToUpdate(foundUser.getWorkContacts().get(institutionInternalId).getEmail(), user.getEmail())) {
            MutableUserFieldsDto dto = mutableUserFieldsDto.orElseGet(MutableUserFieldsDto::new);
            final WorkContact workContact = new WorkContact();
            workContact.setEmail(CertifiedFieldMapper.map(user.getEmail()));
            dto.setWorkContacts(Map.of(institutionInternalId, workContact));
            mutableUserFieldsDto = Optional.of(dto);
        }
        return mutableUserFieldsDto;
    }


    private static boolean isFieldToUpdate(CertifiedField<String> certifiedField, String value) {
        boolean isToUpdate = true;
        if (certifiedField != null) {
            if (Certification.NONE.equals(certifiedField.getCertification())) {
                if (certifiedField.getValue().equals(value)) {
                    isToUpdate = false;
                }
            } else {
                if (certifiedField.getValue().equalsIgnoreCase(value)) {
                    isToUpdate = false;
                } else {
                    throw new UpdateNotAllowedException(String.format("Update user request not allowed because of value %s", value));
                }
            }
        }
        return isToUpdate;
    }


    @Override
    public Collection<InstitutionInfo> getInstitutions(String productId, String userId) {
        log.trace("getInstitutions start");
        Collection<InstitutionInfo> result = partyConnector.getInstitutionsByUser(productId, userId);
        log.debug("getInstitutions result = {}", result);
        log.trace("getInstitutions end");
        return result;
    }


    @Override
    public InstitutionOnboardingData getInstitutionOnboardingDataById(String institutionId, String productId) {
        log.trace("getInstitutionOnboardingData start");
        log.debug("getInstitutionOnboardingData institutionId = {}, productId = {}", Encode.forJava(institutionId), Encode.forJava(productId));
        Assert.hasText(institutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Assert.hasText(productId, A_PRODUCT_ID_IS_REQUIRED);

        List<OnboardingResource> onboardingsResource = partyConnector.getOnboardings(institutionId, productId);
        OnboardingResource onboardingResource = onboardingsResource.stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Onboarding for institutionId %s not found", institutionId)));

        Institution institution = partyConnector.getInstitutionById(institutionId);
        InstitutionOnboardingData result = new InstitutionOnboardingData();
        InstitutionInfo institutionInfo = institutionMapper.toInstitutionInfo(institution);
        institutionInfo.setPricingPlan(onboardingResource.getPricingPlan());
        institutionInfo.setBilling(onboardingResource.getBilling());

        result.setInstitution(institutionInfo);
        result.setGeographicTaxonomies(institution.getGeographicTaxonomies());
        result.setCompanyInformations(institution.getCompanyInformations());
        result.setAssistanceContacts(institution.getAssistanceContacts());

        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionOnboardingData result = {}", result);
        log.trace("getInstitutionOnboardingData end");
        return result;
    }

    @Override
    public Institution getInstitutionByExternalId(String externalInstitutionId) {
        log.trace("getInstitutionData start");
        log.debug("getInstitutionData externalInstitutionId = {}", externalInstitutionId);
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Institution institution = partyConnector.getInstitutionByExternalId(externalInstitutionId);
        log.debug("getInstitutionData result = {}", institution);
        log.trace("getInstitutionData end");
        return institution;
    }

    @Override
    public List<GeographicTaxonomy> getGeographicTaxonomyList(String externalInstitutionId) {
        log.trace("geographicTaxonomyList start");
        log.debug("geographicTaxonomyList externalInstitutionId = {}", externalInstitutionId);
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Institution institution = partyConnector.getInstitutionByExternalId(externalInstitutionId);
        List<GeographicTaxonomy> result = Optional.ofNullable(institution.getGeographicTaxonomies())
                .orElse(Collections.emptyList());
        log.debug("geographicTaxonomyList result = {}", result);
        log.trace("geographicTaxonomyList end");
        return result;
    }

    @Override
    public List<GeographicTaxonomy> getGeographicTaxonomyList(String taxCode, String subunitCode) {

        Assert.hasText(taxCode, REQUIRED_TAX_CODE_MESSAGE);
        List<Institution> institutions = partyConnector.getInstitutionsByTaxCodeAndSubunitCode(taxCode, subunitCode);
        if(Objects.isNull(institutions) || institutions.isEmpty()) return Collections.emptyList();

        return Optional.ofNullable(institutions.get(0).getGeographicTaxonomies())
                .orElse(Collections.emptyList());
    }


    @Override
    public void verifyOnboarding(String externalInstitutionId, String productId) {
        log.trace("verifyOnboarding start");
        log.debug("verifyOnboarding externalInstitutionId = {}", externalInstitutionId);
        validateOnboarding(externalInstitutionId, productId);
        partyConnector.verifyOnboarding(externalInstitutionId, productId);
        log.trace("verifyOnboarding end");
    }


    @Override
    public void verifyOnboarding(String productId, String externalId, String taxCode, String origin, String originId, String subunitCode) {
        log.trace("verifyOnboardingSubunit start");
        log.debug("verifyOnboardingSubunit taxCode = {}", taxCode);
        validateOnboarding(taxCode, productId);
        partyConnector.verifyOnboarding(productId, externalId, taxCode, origin, originId, subunitCode);
        log.trace("verifyOnboardingSubunit end");
    }

    @Override
    public void checkOrganization(String productId, String fiscalCode, String vatNumber) {
        log.trace("checkOrganization start");
        log.debug("checkOrganization productId = {}, fiscalCode = {}, vatNumber = {}", productId, fiscalCode, vatNumber );
        externalInterceptorConnector.checkOrganization(productId, fiscalCode, vatNumber);
        log.trace("checkOrganization end");
    }

    private void validateOnboarding(String externalInstitutionId, String productId) {
        if (!onboardingValidationStrategy.validate(productId, externalInstitutionId)) {
            throw new OnboardingNotAllowedException(String.format(ONBOARDING_NOT_ALLOWED_ERROR_MESSAGE_TEMPLATE,
                    externalInstitutionId,
                    productId));
        }
    }

    @Override
    public InstitutionInfoIC getInstitutionsByUser(String fiscalCode) {
        log.trace("getInstitutionsByUserId start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionsByUserId user = {}", fiscalCode);
        InstitutionInfoIC result = partyRegistryProxyConnector.getInstitutionsByUserFiscalCode(fiscalCode);
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionsByUserId result = {}", result);
        log.trace("getInstitutionsByUserId end");
        return result;
    }

    @Override
    public MatchInfoResult matchInstitutionAndUser(String externalInstitutionId, User user) {
        log.trace("matchInstitutionAndUser start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "matchInstitutionAndUser user = {}", user);
        MatchInfoResult result = partyRegistryProxyConnector.matchInstitutionAndUser(externalInstitutionId, user.getTaxCode());
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "matchInstitutionAndUser result = {}", result);
        log.trace("matchInstitutionAndUser end");
        return result;
    }

    @Override
    public InstitutionLegalAddressData getInstitutionLegalAddress(String externalInstitutionId) {
        log.trace("getInstitutionLegalAddress start");
        log.debug("getInstitutionLegalAddress externalInstitutionId = {}", externalInstitutionId);
        InstitutionLegalAddressData result = partyRegistryProxyConnector.getInstitutionLegalAddress(externalInstitutionId);
        log.debug("getInstitutionLegalAddress result = {}", result);
        log.trace("getInstitutionLegalAddress end");
        return result;
    }

    @Override
    public InstitutionOnboardingData getInstitutionOnboardingData(String externalInstitutionId, String productId) {
        log.trace("getInstitutionOnboardingData start");
        log.debug("getInstitutionOnboardingData externalInstitutionId = {}, productId = {}", externalInstitutionId, productId);
        Assert.hasText(externalInstitutionId, REQUIRED_INSTITUTION_ID_MESSAGE);
        Assert.hasText(productId, A_PRODUCT_ID_IS_REQUIRED);
        InstitutionOnboardingData result = new InstitutionOnboardingData();

        InstitutionInfo institutionInfo = partyConnector.getInstitutionBillingData(externalInstitutionId, productId);
        if (institutionInfo == null) {
            throw new ResourceNotFoundException(String.format("Institution %s not found", externalInstitutionId));
        }

        Institution institution = partyConnector.getInstitutionByExternalId(externalInstitutionId);
        if (institution == null) {
            throw new ResourceNotFoundException(String.format("Institution %s not found", externalInstitutionId));
        }
        if (institution.getGeographicTaxonomies() == null) {
            throw new ValidationException(String.format("The institution %s does not have geographic taxonomies.", externalInstitutionId));
        }
        setInstitutionInfo(institution, institutionInfo);
        setLocationInfo(institutionInfo);
        result.setInstitution(institutionInfo);
        result.setGeographicTaxonomies(institution.getGeographicTaxonomies());
        result.setCompanyInformations(institution.getCompanyInformations());
        result.setAssistanceContacts(institution.getAssistanceContacts());

        log.debug(LogUtils.CONFIDENTIAL_MARKER, "getInstitutionOnboardingData result = {}", result);
        log.trace("getInstitutionOnboardingData end");
        return result;
    }
    private void setInstitutionInfo(Institution institution, InstitutionInfo institutionInfo){
        InstitutionLocation institutionLocation = new InstitutionLocation();
        institutionLocation.setCountry(institution.getCountry());
        institutionLocation.setCity(institution.getCity());
        institutionLocation.setCounty(institution.getCounty());
        institutionInfo.setInstitutionLocation(institutionLocation);
        institutionInfo.setSubunitCode(institution.getSubunitCode());
        institutionInfo.setSubunitType(institution.getSubunitType());
        institutionInfo.setOrigin(institution.getOrigin());
    }

    private void setLocationInfo(InstitutionInfo institutionInfo){
        if (institutionInfo.getInstitutionLocation().getCity()==null && Origin.IPA.getValue().equals(institutionInfo.getOrigin())){
            try {
                GeographicTaxonomies geographicTaxonomies;
                if (institutionInfo.getSubunitType() != null) {
                    geographicTaxonomies = switch (Objects.requireNonNull(institutionInfo.getSubunitType())) {
                        case "UO" -> {
                            OrganizationUnit organizationUnit = partyRegistryProxyConnector.getUoById(institutionInfo.getSubunitCode());
                            yield partyRegistryProxyConnector.getExtById(organizationUnit.getMunicipalIstatCode());
                        }
                        case "AOO" -> {
                            HomogeneousOrganizationalArea homogeneousOrganizationalArea = partyRegistryProxyConnector.getAooById(institutionInfo.getSubunitCode());
                            yield partyRegistryProxyConnector.getExtById(homogeneousOrganizationalArea.getMunicipalIstatCode());
                        }
                        default -> {
                            InstitutionProxyInfo proxyInfo = partyRegistryProxyConnector.getInstitutionProxyById(institutionInfo.getTaxCode());
                            yield partyRegistryProxyConnector.getExtById(proxyInfo.getIstatCode());
                        }
                    };
                }
                else {
                    InstitutionProxyInfo proxyInfo = partyRegistryProxyConnector.getInstitutionProxyById(institutionInfo.getTaxCode());
                    geographicTaxonomies= partyRegistryProxyConnector.getExtById(proxyInfo.getIstatCode());
                }
                if (geographicTaxonomies != null) {
                    institutionInfo.getInstitutionLocation().setCounty(geographicTaxonomies.getProvinceAbbreviation());
                    institutionInfo.getInstitutionLocation().setCountry(geographicTaxonomies.getCountryAbbreviation());
                    institutionInfo.getInstitutionLocation().setCity(geographicTaxonomies.getDescription().replace(DESCRIPTION_TO_REPLACE_REGEX, ""));
                }
            } catch (ResourceNotFoundException e) {
                log.warn("Error while searching institution {} on IPA, {} ", institutionInfo.getDescription(), e.getMessage());
            }
        }
    }

}
