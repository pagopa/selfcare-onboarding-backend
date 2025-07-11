Feature: Institution

  Scenario: Success to complete onboarding
    Given User login with username "j.doe" and password "test"
    And The following request body:
    """
       {
        "assistanceContacts": {
          "supportEmail": "supporto@esempio.it"
        },
        "billingData": {
          "businessName": "Azienda Fittizia S.p.A.",
          "digitalAddress": "azienda.fittizia@pec.it",
          "publicServices": false,
          "recipientCode": "ABC1234",
          "registeredOffice": "Via Roma 123, 84100 Salerno SA",
          "taxCode": "35843432474",
          "taxCodeInvoicing": "12345678901",
          "vatNumber": "IT01234567890",
          "zipCode": "84100"
        },
        "geographicTaxonomies": [
          {
            "code": "084",
            "desc": "Salerno"
          }
        ],
        "institutionLocationData": {
          "city": "Salerno",
          "country": "Italia",
          "county": "Salerno"
        },
        "institutionType": "PRV",
        "isAggregator": false,
        "origin": "SELC",
        "originId": "35843432474",
        "productId": "prod-pagopa",
        "taxCode": "35843432474",
        "users": [
          {
            "email": "mario.rossi@esempio.it",
            "name": "Mario",
            "role": "ADMIN_EA",
            "surname": "Rossi",
            "taxCode": "RSSMRA80A01H703Z"
          }
        ]
      }
    """
    When I send a POST request to "/v1/institutions/onboarding"
    Then The status code is 201

  Scenario: Failed to complete onboarding
    Given User login with username "j.doe" and password "test"
    And The following request body:
    """
       {
        "assistanceContacts": {
          "supportEmail": "supporto@esempio.it"
        },
        "billingData": {
          "businessName": "Azienda Fittizia S.p.A.",
          "digitalAddress": "azienda.fittizia@pec.it",
          "publicServices": false,
          "recipientCode": "ABC1234",
          "registeredOffice": "Via Roma 123, 84100 Salerno SA",
          "taxCode": "35843432474",
          "taxCodeInvoicing": "12345678901",
          "vatNumber": "IT01234567890",
          "zipCode": "84100"
        },
        "geographicTaxonomies": [
          {
            "code": "084",
            "desc": "Salerno"
          }
        ],
        "institutionLocationData": {
          "city": "Salerno",
          "country": "Italia",
          "county": "Salerno"
        },
        "institutionType": "PRV",
        "isAggregator": false,
        "origin": "SELC",
        "originId": "35843432474",
        "productId": "prod-pagopa",
        "taxCode": "35843432474",
        "users": [
          {
            "email": "mario.rossi@esempio.it",
            "name": "Example",
            "role": "ADMIN_EA",
            "surname": "Exampleee",
            "taxCode": "RSSMRA80A01H703Z"
          }
        ]
      }
    """
    When I send a POST request to "/v1/institutions/onboarding"
    Then The status code is 409

  Scenario: Success to retrive onboarding
    Given User login with username "j.doe" and password "test"
    When I send a GET request to "/v1/institutions/onboarding?institutionId=test1323&productId=prod-io"
    Then The status code is 201