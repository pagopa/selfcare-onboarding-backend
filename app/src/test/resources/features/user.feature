Feature: User

  Scenario: Success to validate user from PDV
    Given User login with username "j.doe" and password "test"
    And The following request body:
    """
      {
        "taxCode": "VRDMRA22T71F205A",
        "name":"Tizio",
        "surname":"Caio"
      }
    """
    When I send a POST request to "/v1/users/validate"
    Then The status code is 204

  Scenario: Failed to validate user from PDV error name
    Given User login with username "j.doe" and password "test"
    And The following request body:
    """
      {
        "taxCode": "VRDMRA22T71F205A",
        "name":"Tizioz",
        "surname":"Caio"
      }
    """
    When I send a POST request to "/v1/users/validate"
    Then The status code is 409


  Scenario: Failed to validate user from PDV error surname
    Given User login with username "j.doe" and password "test"
    And The following request body:
    """
      {
        "taxCode": "VRDMRA22T71F205A",
        "name":"Tizio",
        "surname":"Caioz"
      }
    """
    When I send a POST request to "/v1/users/validate"
    Then The status code is 409

  Scenario: Failed to validate user from PDV when not exist
    Given User login with username "j.doe" and password "test"
    And The following request body:
    """
      {
        "taxCode": "VRDMRA22T71F205X",
        "name":"Tizio",
        "surname":"Caio"
      }
    """
    When I send a POST request to "/v1/users/validate"
    Then The status code is 404


  Scenario: Success to search user from PDV
    Given User login with username "j.doe" and password "test"
    And The following request body:
    """
      {
        "taxCode": "VRDMRA22T71F205A"
      }
    """
    When I send a POST request to "/v1/users/search-user"
    Then The status code is 200

  Scenario: Failed to search user from PDV
    Given User login with username "j.doe" and password "test"
    And The following request body:
    """
      {
        "taxCode": "VRDMRA22T71F205X"
      }
    """
    When I send a POST request to "/v1/users/search-user"
    Then The status code is 404