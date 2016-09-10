Feature: Piper
  As a mosaic shop
  I want to have Piper
  So that I can deliver pages to customers

  Background:
    Given some fragments

  Scenario: Puts together a simple template
    Given a default piper app
    When I do a request to the piper app
    Then I should get the correct html page

  Scenario: Puts together a simple template without a primary fragment
    Given a piper app without a primary fragment
    When I do a request to the piper app
    Then I should get the correct html page

  Scenario: Handles 500 from the primary fragment
    Given a piper app with a primary fragment which returns 500
    When I do a request to the piper app
    Then I should get an error

  Scenario: Handles timeout from the primary fragment
    Given a piper app with a primary fragment which returns a timeout
    When I do a request to the piper app
    Then I should get an error

  Scenario: Handles timeout from a fragment
    Given a piper app with a fragment which returns a timeout
    When I do a request to the piper app
    Then the timed out fragment content should not be included
