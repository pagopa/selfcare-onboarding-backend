Feature: Token

  Scenario: Success to complete onboarding
    Given User login with username "j.doe" and password "test"
    And A mock-file of type "application/pdf" with key "contract" and document path "mock/mock-template.pdf" used to perform request
    When I send a POST request to "/v2/tokens/89ad7142-24bb-48ad-8504-9c9231137i103/complete" with multi-part file
    Then The status code is 204

  Scenario: Failed to complete onboarding when onboarding is missing
    Given User login with username "j.doe" and password "test"
    And A mock-file of type "application/pdf" with key "contract" and document path "mock/mock-template.pdf" used to perform request
    When I send a POST request to "/v2/tokens/89ad7142-24bb-48ad-8504-9c9231137i104/complete" with multi-part file
    Then The status code is 400
    And The response body contains field "errors[0].detail" with value "Onboarding with id 89ad7142-24bb-48ad-8504-9c9231137i104 not found or it is expired!"
