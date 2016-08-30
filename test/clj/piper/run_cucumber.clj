(ns piper.run-cucumber
  (:require [clojure.test :refer [deftest]])
  (:import (cucumber.api.cli Main)))

(deftest run-cukes
  (let [classloader (.getContextClassLoader (Thread/currentThread))]
    (. Main (run
              (into-array ["--plugin"
                           "pretty"
                           "--glue"
                           "test/acceptance/step_definitions"
                           "test/acceptance/features"])
              classloader))))
