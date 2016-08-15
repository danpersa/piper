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
  :main ^:skip-aot piper.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
