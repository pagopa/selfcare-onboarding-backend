package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.commons.base.logging.LogUtils;
import it.pagopa.selfcare.onboarding.connector.api.OnboardingMsConnector;
import it.pagopa.selfcare.onboarding.connector.api.UserRegistryConnector;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.OnboardingData;
import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;
import it.pagopa.selfcare.onboarding.connector.model.user.Certification;
import it.pagopa.selfcare.onboarding.connector.model.user.CertifiedField;
import it.pagopa.selfcare.onboarding.core.exception.InvalidUserFieldsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Optional;

import static it.pagopa.selfcare.onboarding.connector.model.user.User.Fields.familyName;
import static it.pagopa.selfcare.onboarding.connector.model.user.User.Fields.name;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final EnumSet<it.pagopa.selfcare.onboarding.connector.model.user.User.Fields> FIELD_LIST = EnumSet.of(name, familyName);
    private static final String INVALID_FIELD_REASON = "the value does not match with the certified data";
    private final UserRegistryConnector userRegistryConnector;
    private final OnboardingMsConnector onboardingMsConnector;

    @Autowired
    public UserServiceImpl(UserRegistryConnector userRegistryConnector,
                           OnboardingMsConnector onboardingMsConnector) {
        this.userRegistryConnector = userRegistryConnector;
        this.onboardingMsConnector = onboardingMsConnector;
    }

    @Override
    public void validate(User user) {
        log.trace("validate start");
        log.debug(LogUtils.CONFIDENTIAL_MARKER, "validate user = {}", user);
        Assert.notNull(user, "An user is required");
        final Optional<it.pagopa.selfcare.onboarding.connector.model.user.User> searchResult =
                userRegistryConnector.search(user.getTaxCode(), FIELD_LIST);
        searchResult.ifPresent(foundUser -> {
            final ArrayList<InvalidUserFieldsException.InvalidField> invalidFields = new ArrayList<>();
            if (!isValid(user.getName(), foundUser.getName())) {
                invalidFields.add(new InvalidUserFieldsException.InvalidField("name", INVALID_FIELD_REASON));
            }
            if (!isValid(user.getSurname(), foundUser.getFamilyName())) {
                invalidFields.add(new InvalidUserFieldsException.InvalidField("surname", INVALID_FIELD_REASON));
            }
            if (!invalidFields.isEmpty()) {
                throw new InvalidUserFieldsException(invalidFields);
            }
        });
        log.trace("validate end");
    }

    @Override
    public void onboardingUsers(OnboardingData onboardingData) {
        log.trace("onboardingUsers start");
        log.debug("onboardingUsers onboardingData = {}", onboardingData);
        onboardingMsConnector.onboardingUsers(onboardingData);
        log.trace("onboardingUsers end");
    }

    @Override
    public void onboardingUsersAggregator(OnboardingData onboardingData) {
        log.trace("onboardingUsersAggregator start");
        log.debug("onboardingUsersAggregator onboardingData = {}", onboardingData);
        onboardingMsConnector.onboardingUsersAggregator(onboardingData);
        log.trace("onboardingUsersAggregator end");
    }

    @Override
    public boolean  checkManager(OnboardingData onboardingData) {
        log.trace("checkManager start");
        log.debug("checkManager onboardingData = {}", onboardingData);
        boolean checkManager =  onboardingMsConnector.checkManager(onboardingData);
        log.trace("checkManager end");
        return checkManager;
    }

    private <T> boolean isValid(T field, CertifiedField<T> certifiedField) {
        return certifiedField == null
                || !Certification.isCertified(certifiedField.getCertification())
                || (String.class.isAssignableFrom(certifiedField.getValue().getClass())
                ? ((String) certifiedField.getValue()).equalsIgnoreCase((String) field)
                : certifiedField.getValue().equals(field));
    }

}
