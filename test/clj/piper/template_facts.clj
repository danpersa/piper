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

(def ^:private ast [{:text "<html>\n<body>\n<div>Hello</div>\n"}
                    {:fragment {:src "http://localhost:8083/fragment-1" :id 1}}
                    {:text "\n<div>Hello1</div>\n"}
                    {:fragment {:src "http://localhost:8083/fragment-2" :primary nil :id 2}}
                    {:text "\n</body>\n</html>"}])

(facts "parse-template"
       (parse-template template) => ast)

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

(def ^:private primary-fragments [{:fragment {:id 1 :src "http://localhost:8083/fragment-1"}}
                                  {:fragment {:id 2 :primary nil :src "http://localhost:8083/fragment-2"}}])

(def ^:private no-primary-fragments [{:fragment {:id 1 :src "http://localhost:8083/fragment-1"}}
                                     {:fragment {:id 2 :src "http://localhost:8083/fragment-2"}}])

(facts "fragment-nodes"
       (fragment-nodes ast) => primary-fragments)

(facts "select-primary"
       (fact "when primary is present"
             (select-primary primary-fragments) =>
             {:primary   {:id 2 :primary nil :src "http://localhost:8083/fragment-2"}
              :fragments [{:id 1 :src "http://localhost:8083/fragment-1"}]})
       (fact "when primary is not present"
             (select-primary no-primary-fragments) =>
             {:fragments [{:id 1 :src "http://localhost:8083/fragment-1"}
                          {:id 2 :src "http://localhost:8083/fragment-2"}]}))
