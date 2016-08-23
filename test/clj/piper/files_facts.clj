(ns piper.files-facts
  (:require [midje.sweet :refer :all]
            [piper.files :refer :all]
            [clojure.string :as str]))

(facts "classpath-file-as-str"
       (classpath-file-as-str "templates/simple-template.html") =>
       (str/join "\n"
                 ["<html>"
                  "<body>"
                  "<div>Hello</div>"
                  "<fragment src=\"http://localhost:8083/fragment-1\"></fragment>"
                  "<slot name=\"body-start\"></slot>"
                  "<fragment src=\"http://localhost:8083/fragment-2\" primary></fragment>"
                  "</body>"
                  "</html>"]))
