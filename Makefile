
acceptance-tests:
	lein cucumber --glue test/acceptance/step_definitions --plugin pretty

run-spec:
	lein spec spec -f d

watch-cucumber:
	lein test-refresh

watch-midje:
	lein midje :autotest
