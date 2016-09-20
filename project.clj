(defproject piper "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure           "1.9.0-alpha11"]
                 [org.clojure/core.async        "0.2.385"]
                 [org.immutant/web              "2.1.5"]
                 [instaparse                    "1.4.2"]
                 [org.clojure/core.match        "0.3.0-alpha4"]
                 [defun                         "0.3.0-alapha"]
                 [org.clojure/tools.logging     "0.3.1"]
                 [core.async.http.client        "0.1.0-SNAPSHOT"]]
  :test-paths ["test/acceptance/features" "test/acceptance/step_definitions" "test/clj"]
  :cucumber-feature-paths ["test/acceptance/features"]
  :main ^:skip-aot piper.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev     {:source-paths   ["dev"]
                       :resource-paths ["test/resources"]
                       :plugins        [[lein-midje                           "3.2"]
                                        [org.clojars.punkisdead/lein-cucumber "1.0.7"]
                                        [com.jakemccrary/lein-test-refresh    "0.16.0"]
                                        [speclj                               "3.3.2"]]
                       :dependencies   [[midje                       "1.9.0-alpha5"]
                                        [info.cukes/cucumber-clojure "1.2.4"]
                                        [org.clojure/data.json       "0.2.6"]
                                        [speclj                      "3.3.2"]]
                       :test-refresh   {:growl             true
                                        :notify-on-success true
                                        :changes-only      true
                                        :watch-dirs        ["src" "test"]
                                        :refresh-dirs      ["src" "test"]}}})
