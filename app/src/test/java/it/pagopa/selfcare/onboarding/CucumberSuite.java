package it.pagopa.selfcare.onboarding;

import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import io.cucumber.spring.CucumberContextConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@CucumberContextConfiguration
@SpringBootTest(
    classes = {SelfCareOnboardingApplication.class, RestAssuredConfiguration.class},
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
/*
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME,
    value = "it.pagopa.selfcare.cucumber.utils,it.pagopa.selfcare.onboarding.integration_test")
 */
@Slf4j
public class CucumberSuite {

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      String publicKey;
      try (InputStream inputStream = classLoader.getResourceAsStream("key/public-key.pub")) {
          if (inputStream == null) {
            throw new IOException("Public key file not found in classpath");
          }
          publicKey = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
      }
      registry.add("JWT_TOKEN_PUBLIC_KEY", () -> publicKey);
  }
}
