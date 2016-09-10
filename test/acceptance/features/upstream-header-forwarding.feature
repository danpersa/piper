Feature: Upstream Header Forwarding
  As piper
  I want to forward the headers from upstream to the fragments
  So that fragments can use those headers to make decisions

  Background:
    Given some fragments

  Scenario: Forwards the headers it gets from upstream to fragments
    Given a piper app with a fragment which returns the x-headers it gets
    When I do a request to the piper app with the header name "x-some" and value "hello"
    Then I should get the body "<html><div>x-some: hello</div></html>"
