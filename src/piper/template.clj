(ns piper.template
  (:require [instaparse.core :as in]
            [instaparse.transform :as tr]
            [clojure.core.match :refer [match]]
            [piper.files :as fs]
            [defun :refer [defun fun]]
            [clojure.string :as str]))

(def template (fs/classpath-file-as-str "template-1.html"))

(def parser (in/parser
              "
              tags             = (tag | close-tag | closed-tag | text)*
              close-tag        = <'</'> name <'>'>
              closed-tag       = <'<'> name attr* <'/>'>
              tag              = <'<'> name attr* <'>'>
              attr              = attr-with-value | attr-no-value
              attr-with-value   = name <'='> attr-value
              attr-no-value     = name
              attr-value        = <quote> #'[^\"]*' <quote>
              quote            = #'\"'
              name             = #'[a-zA-Z0-9!-]+'
              text             = #'[^<]*'
              "
              :auto-whitespace :standard
              :output-format :hiccup))

(defun attr-to-string
       ([{:name name :value nil}] name)
       ([{:name name :value value}] (str name "=\"" value "\"")))

(defn attrs-to-string [attrs]
  (->> attrs
       (map (fn [attr] (attr-to-string attr)))
       (str/join " ")))

(into {} [[:a 1] [:b 3]])

(defn attrs-to-map [attrs]
  (->> (map (fn [attr] [(keyword (:name attr)) (:value attr)]) attrs)
       (into {})))

(defn add-leading-space [s]
  (if (empty? s)
    s
    (str " " s)))

(defn assoc-id [attrs id]
  (assoc attrs :id id))

(def attrs [{:name "n1" :value "v1"} {:name "n3" :value nil} {:name "n2" :value "v2"}])
(attrs-to-string attrs)
(attrs-to-map attrs)

(assoc-id (attrs-to-map attrs) 2)


(defn parse-template
  "Takes an html template as a parameter. Returns the AST."
  [template]
  (let [id (atom 0)
        nodes (tr/transform {:tags            (fun ([& rest] rest))
                             :close-tag       (fun
                                                ([[:name (:or "fragment" "slot")]]
                                                  nil)
                                                ([[:name close-tag-name]]
                                                  {:text (str "</" close-tag-name ">")}))
                             :text            (fn [text] {:text text})
                             :attr            (fn [attr] attr)
                             :attr-with-value (fun ([[:name name] [:attr-value attr-value]] {:name  name
                                                                                             :value attr-value}))
                             :attr-no-value   (fun ([[:name name]] {:name  name
                                                                    :value nil}))
                             :tag             (fun ([[:name "slot"] & attrs] {:slot (attrs-to-map attrs)})
                                                   ([[:name "fragment"] & attrs] {:fragment (assoc-id (attrs-to-map attrs) (swap! id inc))})
                                                   ([[:name tag-name] & attrs]
                                                     {:text (str "<" tag-name (add-leading-space
                                                                                (attrs-to-string attrs)) ">")}))
                             :closed-tag      (fun ([[:name "slot"] & attrs] {:slot (attrs-to-map attrs)})
                                                   ([[:name "fragment"] & attrs] {:fragment (assoc-id (attrs-to-map attrs) (swap! id inc))})
                                                   ([[:name tag-name] & attrs]
                                                     {:text (str "<" tag-name (add-leading-space
                                                                                (attrs-to-string attrs)) "/>")}))}
                            (parser template))]

    (reduce (fn [result next]
              (let [last (last result)]
                (match [last next]
                       [{:text t1} {:text t2}] (conj (vec (drop-last result)) {:text (str t1 t2)})
                       [last nil] result
                       [last next] (conj result next))))
            []
            nodes)))

(defn fragment-nodes
  "Takes the AST as a parameter. Returns only the fragment nodes"
  [ast]
  (filter #(some? (%1 :fragment)) ast))

(defn select-primary
  "Takes a list of fragment nodes. Returns a map with the primary fragment and the other fragments."
  [fragment-nodes]
  (loop [nodes fragment-nodes
         result {:fragments []}]
    (if-some [fragment-node (:fragment (first nodes))]
      (let [new-result (match fragment-node
                              {:primary _} (assoc result :primary fragment-node)
                              :else (assoc result :fragments (conj (result :fragments) fragment-node)))]
        ;(println "new result " new-result)
        (recur (rest nodes) new-result))
      result)))


(comment
  (parser "<hello>")
  (parser "<hello src=\"hello\"/>")
  (parser "<hello src=\"hello\">")
  (parser "<hello>world >>> xx bb</hello>")
  (parser template)
  (select-primary (fragment-nodes (parse-template template)))


  (render-channels (ast-to-channels (parse-template template))))

