(ns piper.template-facts
  (:require [midje.sweet :refer :all]
            [midje.util :refer [testable-privates]]
            [piper.template :refer :all]
            [piper.files :as fs]))

(testable-privates piper.template
                   parser
                   attr->string
                   attrs->string
                   attrs->map
                   prepend-space
                   assoc-id)

(facts "parser"
       (parser "<hello>") => [:tags [:tag [:name "hello"]]]
       (parser "<hello src=\"hello\"/>") => [:tags [:closed-tag [:name "hello"]
                                                    [:attr [:attr-with-value [:name "src"]
                                                            [:attr-value "hello"]]]]]
       (parser "<hello src=\"hello\">") => [:tags [:tag [:name "hello"]
                                                   [:attr [:attr-with-value [:name "src"]
                                                           [:attr-value "hello"]]]]]
       (parser "<hello>world >>> xx bb</hello>") => [:tags [:tag [:name "hello"]]
                                                     [:text "world >>> xx bb"]
                                                     [:close-tag [:name "hello"]]])

(def ^:private template (fs/classpath-file-as-str
                          "templates/simple-template.html"))

(facts "parse-template"
       (parse-template template) =>
       [{:text "<html>\n<body>\n<div>Hello</div>\n"}
        {:slot {:name "body-start"}}
        {:text "\n"}
        {:fragment {:src "http://localhost:8083/fragment-2", :primary nil, :id 1}}
        {:text "\n</body>\n</html>"}])

;(def ^:private attrs [{:name "n1" :value "v1"} {:name "n3" :value nil} {:name "n2" :value "v2"}])

(facts "attr->string"
       (attr->string {:name "n1" :value "v1"}) => "n1=\"v1\""
       (attr->string {:name "n1" :value nil}) => "n1")

(facts "attrs->string"
       (attrs->string [{:name "n1" :value "v1"} {:name "n3" :value nil}])
       => "n1=\"v1\" n3")

(facts "attrs->map"
       (attrs->map [{:name "n1" :value "v1"} {:name "n3" :value nil}])
       => {:n1 "v1" :n3 nil})

(facts "prepend-space"
       (prepend-space "") => ""
       (prepend-space "hello") => " hello")

(facts "assoc-id"
       (assoc-id {:a1 1} 2) => {:a1 1 :id 2})
