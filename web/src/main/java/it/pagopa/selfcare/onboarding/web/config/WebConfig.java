package it.pagopa.selfcare.onboarding.web.config;

import it.pagopa.selfcare.commons.web.config.BaseWebConfig;
import it.pagopa.selfcare.commons.web.handler.RestExceptionsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collection;

@Configuration
@Import(RestExceptionsHandler.class)
class WebConfig extends BaseWebConfig {

    @Autowired
    WebConfig(Collection<HandlerInterceptor> interceptors) {
        super(interceptors);
    }

}
