package it.pagopa.selfcare.onboarding.connector.rest.config;

import it.pagopa.selfcare.onboarding.connector.rest.client.MsExternalInterceptorApiClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableFeignClients(clients = {MsExternalInterceptorApiClient.class})
@PropertySource("classpath:config/ms-external-interceptor-api-client.properties")
public class MsExternalInterceptorApiClientConfig {
}
