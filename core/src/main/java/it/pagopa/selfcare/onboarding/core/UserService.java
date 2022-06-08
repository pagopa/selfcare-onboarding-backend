package it.pagopa.selfcare.onboarding.core;

import it.pagopa.selfcare.onboarding.connector.model.onboarding.User;

public interface UserService {

    void validate(User user);
}
