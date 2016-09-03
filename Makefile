
acceptance-tests:
	lein cucumber --glue test/acceptance/step_definitions --plugin pretty

watch-cucumber:
	lein test-refresh

watch-midje:
	lein midje :autotest
