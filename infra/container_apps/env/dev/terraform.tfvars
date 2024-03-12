env_short = "d"

tags = {
  CreatedBy   = "Terraform"
  Environment = "Dev"
  Owner       = "SelfCare"
  Source      = "https://github.com/pagopa/selfcare-onboarding-backend"
  CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
}

container_app = {
  min_replicas = 0
  max_replicas = 1
  scale_rules  = []
  cpu          = 0.5
  memory       = "1Gi"
}

app_settings = [
  {
    name  = "APPLICATIONINSIGHTS_ROLE_NAME"
    value = "b4f-onboarding",
  },
  {
    name  = "JAVA_TOOL_OPTIONS"
    value = "-javaagent:applicationinsights-agent.jar",
  },
  {
    name  = "APPLICATIONINSIGHTS_INSTRUMENTATION_LOGGING_LEVEL"
    value = "OFF",
  },
  {
    name  = "B4F_ONBOARDING_LOG_LEVEL"
    value = "DEBUG",
  },
  {
    name  = "REST_CLIENT_LOGGER_LEVEL"
    value = "FULL",
  },
  {
    name  = "MS_ONBOARDING_URL"
    value = "https://selc-d-onboarding-ms-ca.gentleflower-c63e62fe.westeurope.azurecontainerapps.io",
  },
  {
    name  = "ONBOARDING_ALLOWED_INSTITUTIONS_PRODUCTS"
    value = "{'prod-interop':{'*'},'prod-pn':{'*'},'prod-io':{'*'},'prod-io-premium':{'*'},'prod-pagopa':{'*'},'prod-fd':{'*'},'prod-fd-garantito':{'*'},'prod-io-sign':{'*'},'prod-sendino':{'*'}}"
  },
  {
    name  = "MS_CORE_URL"
    value = "https://selc-d-ms-core-ca.gentleflower-c63e62fe.westeurope.azurecontainerapps.io"
  },
  {
    name  = "MS_EXTERNAL_INTERCEPTOR_URL"
    value = "https://selc-d-ms-external-api-backend-ca.gentleflower-c63e62fe.westeurope.azurecontainerapps.io"
  },
  {
    name  = "USERVICE_PARTY_PROCESS_URL"
    value = "https://selc-d-ms-core-ca.gentleflower-c63e62fe.westeurope.azurecontainerapps.io"
  },
  {
    name  = "USERVICE_PARTY_REGISTRY_PROXY_URL"
    value = "https://selc-d-party-reg-proxy-ca.gentleflower-c63e62fe.westeurope.azurecontainerapps.io"
  },
  {
    name  = "MS_PRODUCT_URL"
    value = "http://selc.internal.dev.selfcare.pagopa.it/ms-product/v1"
  },
  {
    name  = "USERVICE_USER_REGISTRY_URL"
    value = "https://api.uat.pdv.pagopa.it/user-registry/v1"
  }
]

secrets_names = {
  "USERVICE_USER_REGISTRY_API_KEY"        = "user-registry-api-key"
  "APPLICATIONINSIGHTS_CONNECTION_STRING" = "appinsights-connection-string"
  "JWT_TOKEN_PUBLIC_KEY"                  = "jwt-public-key"
}
