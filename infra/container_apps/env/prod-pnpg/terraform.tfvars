is_pnpg             = true
env_short           = "p"
private_dns_name    = "selc-p-pnpg-onboardingbackend-ca.redbeach-5da5166f.westeurope.azurecontainerapps.io"
dns_zone_prefix     = "imprese.notifichedigitali"
api_dns_zone_prefix = "api-pnpg.selfcare"
external_domain     = "it"

tags = {
  CreatedBy   = "Terraform"
  Environment = "Prod"
  Owner       = "SelfCare"
  Source      = "https://github.com/pagopa/selfcare-onboarding-backend"
  CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
}

container_app = {
  min_replicas = 1
  max_replicas = 5
  scale_rules = [
    {
      custom = {
        metadata = {
          "desiredReplicas" = "3"
          "start"           = "0 8 * * MON-FRI"
          "end"             = "0 19 * * MON-FRI"
          "timezone"        = "Europe/Rome"
        }
        type = "cron"
      }
      name = "cron-scale-rule"
    }
  ]
  cpu    = 1.25
  memory = "2.5Gi"
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
    value = "INFO",
  },
  {
    name  = "REST_CLIENT_LOGGER_LEVEL"
    value = "BASIC",
  },
  {
    name  = "MS_ONBOARDING_URL"
    value = "https://selc-p-pnpg-onboarding-ms-ca.redbeach-5da5166f.westeurope.azurecontainerapps.io",
  },
  {
    name  = "ONBOARDING_ALLOWED_INSTITUTIONS_PRODUCTS"
    value = "{'prod-pn-pg':{'*'}}"
  },
  {
    name  = "REST_CLIENT_READ_TIMEOUT"
    value = "60000"
  },
  {
    name  = "REST_CLIENT_CONNECT_TIMEOUT"
    value = "60000"
  },
  {
    name  = "MS_CORE_URL"
    value = "https://selc-p-pnpg-ms-core-ca.redbeach-5da5166f.westeurope.azurecontainerapps.io"
  },
  {
    name  = "MS_EXTERNAL_INTERCEPTOR_URL"
    value = "https://selc-p-pnpg-ext-interceptor-ca.redbeach-5da5166f.westeurope.azurecontainerapps.io"
  },
  {
    name  = "USERVICE_PARTY_PROCESS_URL"
    value = "https://selc-p-pnpg-ms-core-ca.redbeach-5da5166f.westeurope.azurecontainerapps.io"
  },
  {
    name  = "USERVICE_PARTY_REGISTRY_PROXY_URL"
    value = "https://selc-p-pnpg-party-reg-proxy-ca.redbeach-5da5166f.westeurope.azurecontainerapps.io"
  },
  {
    name  = "MS_PRODUCT_URL"
    value = "https://selc-p-pnpg-product-ca.redbeach-5da5166f.westeurope.azurecontainerapps.io"
  },
  {
    name  = "USERVICE_USER_REGISTRY_URL"
    value = "https://api.pdv.pagopa.it/user-registry/v1"
  }
]

secrets_names = {
  "USERVICE_USER_REGISTRY_API_KEY"        = "user-registry-api-key"
  "APPLICATIONINSIGHTS_CONNECTION_STRING" = "appinsights-connection-string"
  "JWT_TOKEN_PUBLIC_KEY"                  = "jwt-public-key"
}