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
                 [cc.qbits/jet             "0.7.9"]]
  :main ^:skip-aot piper.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
