locals {
    apim_name = format("selc-%s-apim", var.env_short)
    apim_rg   = format("selc-%s-api-rg", var.env_short)
    api_name  = format("selc-%s-api-bff-onboarding", var.env_short)
}


resource "azurerm_api_management_api_version_set" "apim_api_bff_onboarding" {
  name                = local.api_name
  resource_group_name = local.apim_rg
  api_management_name = local.apim_name
  display_name        = "BFF Onboarding API"
  versioning_scheme   = "Segment"
}


module "apim_api_bff_onboarding" {
  source              = "github.com/pagopa/terraform-azurerm-v3.git//api_management_api?ref=v7.50.1"
  name                = local.api_name
  api_management_name = local.apim_name
  resource_group_name = local.apim_rg
  version_set_id      = azurerm_api_management_api_version_set.apim_api_bff_onboarding.id

  description  = "BFF Onboarding API"
  display_name = "BFF Onboarding API"
  path         = "onboarding"
  protocols = [
    "https"
  ]

  service_url = format("https://%s", var.private_dns_name)

  content_format = "openapi+json"
  content_value  = templatefile("../../app/src/main/resources/swagger/api-docs.json", {
    url         = format("%s.%s", var.api_dns_zone_prefix, var.external_domain)
    basePath     = "/onboarding"
  })

  subscription_required = false

  xml_content = <<XML
<policies>
    <inbound>
        <cors>
            <allowed-origins>
                <origin>https://${var.dns_zone_prefix}.${var.external_domain}</origin>
                <origin>https://${var.api_dns_zone_prefix}.${var.external_domain}</origin>
                <origin>https://localhost:3000</origin>
            </allowed-origins>
            <allowed-methods>
                <method>GET</method>
                <method>POST</method>
                <method>PUT</method>
                <method>DELETE</method>
                <method>HEAD</method>
                <method>OPTIONS</method>
            </allowed-methods>
            <allowed-headers>
                <header>*</header>
            </allowed-headers>
        </cors>
        <base />
    </inbound>
    <backend>
        <base />
    </backend>
    <outbound>
        <base />
    </outbound>
    <on-error>
        <base />
    </on-error>
</policies>
XML
}
