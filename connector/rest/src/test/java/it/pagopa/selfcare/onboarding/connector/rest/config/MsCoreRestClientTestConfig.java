package it.pagopa.selfcare.onboarding.connector.rest.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(MsCoreRestClientConfig.class)
public class MsCoreRestClientTestConfig {
}