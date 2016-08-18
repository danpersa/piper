(defproject piper "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure      "1.9.0-alpha10"]
                 [org.clojure/core.async   "0.2.385"]
                 [org.immutant/web         "2.1.5"]
                 [clj-http                 "3.1.0"]
                 [http.async.client        "1.1.0"]
                 [instaparse               "1.4.2"]
                 [org.clojure/core.match   "0.3.0-alpha4"]
                 [defun                    "0.3.0-alapha"]
                 [org.clojure/tools.logging "0.3.1"]]
  :test-paths ["test/acceptance/features" "test/acceptance/step_definitions" "test/clj"]
  :cucumber-feature-paths ["test/acceptance/features"]
  :main ^:skip-aot piper.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:source-paths ["dev"]
                   :plugins      [[lein-midje                           "3.2"]
                                  [org.clojars.punkisdead/lein-cucumber "1.0.7"]]
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [org.clojure/java.classpath  "0.2.3"]
                                  [criterium                   "0.4.4"]
                                  [midje                       "1.9.0-alpha4"]
                                  [info.cukes/cucumber-clojure "1.2.4"]]}})
