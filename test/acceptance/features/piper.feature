Feature: Piper
  As a mosaic shop
  I want to have Piper
  So that I can deliver pages to customers

  Scenario: Start the Piper
    Given some fragments
    And a default piper app
    When I do a request for the default template
    Then I should get the correct html page