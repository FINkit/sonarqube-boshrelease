Feature: sonarqube configuration
  In order have a valid sonarqube installation
  As an administrator
  I need to be able to login to sonarqube

  Scenario: Can access login screen
    Given there is a sonarqube install
    When I access the login screen
    Then sonarqube should be unlocked
