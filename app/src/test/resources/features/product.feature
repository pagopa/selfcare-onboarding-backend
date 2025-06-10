Feature: Product

  Scenario: Success in getProducts
    Given User login with username "j.doe" and password "test"
    When I send a GET request to "/v1/products"
    Then The status code is 200
    And The response body contains the list "status" of size 14
