Feature: sonarqube authentication and authorisation
  In order to have a valid sonarqube authentication
  As a sonarqube user
  I am able to view the assigned project

  @unit
  Scenario: Can login as test standalone user
    Given there is a sonarqube install
    When I access the login screen
    Then I am able to login

  @unit 
  Scenario: Unable to login without valid credentials
    Given there is a sonarqube install
    When I access the login screen
    Then I am unable to login

  @integration
  Scenario: Unable to login as anonymous user
    Given there is a sonarqube install
    When I access the login screen
    Then I am presented with a Github login option
