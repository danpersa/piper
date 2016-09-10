Feature: Upstream Header Forwarding
  As piper
  I want to forward the headers from upstream to the fragments
  So that fragments can use those headers to make decisions

  Background:
    Given some fragments

  Scenario: Forwards the headers it gets from upstream to fragments
    Given a piper app with a fragment which returns the x-headers it gets
    And I prepare the header with name "x-header-1" and value "value-1"
    And I prepare the header with name "x-header-2" and values "value-2" and "value-3"
    When I do a request to the piper app
    Then I should get the body "<html><div>x-header-1: value-1</div><div>x-header-2: value-2,value-3</div></html>"
