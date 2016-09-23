(ns piper.run-cucumber
  (:require [clojure.test :refer [deftest]])
  (:import (cucumber.api.cli Main)))

(comment
  (let [classloader (.getContextClassLoader (Thread/currentThread))]
    (. Main (run
              (into-array ["--plugin"
                           "pretty"
                           "--glue"
                           "test/acceptance/step_definitions"
                           "test/acceptance/features"])
              classloader))))
