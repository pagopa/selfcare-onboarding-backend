Feature: ProductMS

  Scenario: Success in getOrigins
    Given User login with username "j.doe" and password "test"
    And The following query params:
      | productId | prod-test |
    When I send a GET request to "/v2/product/origins"
    Then The status code is 200
    And The response body contains:
      | origins[0].institutionType |                               |
      | origins[0].origin          | Description updated via PATCH |
      | origins[0].labelKey        | false                         |