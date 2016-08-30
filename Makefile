
acceptance-tests:
	lein cucumber --glue test/acceptance/step_definitions

watch-cucumber:
	lein test-refresh

watch-midje:
	lein midje :autotest
