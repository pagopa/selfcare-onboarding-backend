package it.pagopa.selfcare.onboarding.connector.rest.config;

import it.pagopa.selfcare.commons.connector.rest.config.RestClientBaseConfig;
import it.pagopa.selfcare.onboarding.connector.rest.client.UserRegistryRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;


@Configuration
@Import({RestClientBaseConfig.class})
@EnableFeignClients(clients = UserRegistryRestClient.class)
@PropertySource("classpath:config/user-registry-rest-client.properties")
public class UserRegistryRestClientConfig {

    @Value("${rest-client.user-registry.apiKey}")
    private String apiKey;

    @Bean
    public ApiKeyRequestInterceptor userRegistryApiKeyInterceptor() {
        return new ApiKeyRequestInterceptor(apiKey);
    }
}
