# piper

FIXME: description

## Installation

Download from http://example.com/FIXME.

## Usage

FIXME: explanation

    $ java -jar piper-0.1.0-standalone.jar [args]

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Development

`lein test-refresh` to start the test watcher

## Links

    https://github.com/gphilipp/bdd-guide-clojure

## Run Cucumber from IntelliJ
```
Main Class: cucumber.api.cli.Main
Glue: test/acceptance/step_definitions
Feature Folder: .... piper/test/acceptance/features (Use full path)
VM Options: -Xbootclasspath/p:test/acceptance/step_definitions:src:test/clj:test/resources
Program Arguments: "--plugin" "pretty"
```

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
