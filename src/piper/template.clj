(ns piper.templatei
  (:require [instaparse.core :as in]
            [instaparse.transform :as tr]
            [clojure.core.match :refer [match]]
            [defun :refer [defun fun]]
            [clojure.string :as str]))

(def template "<html>
  <head>
    <script type=\"fragment\" src=\"http://assets.domain.com\" attr></script>
  </head>
  <body>
    <slot name=\"body-start\"></slot>
    <fragment src=\"http://header.domain.com\"></fragment>
    <fragment src=\"http://content.domain.com\" primary></fragment>
    <fragment src=\"http://footer.domain.com\" async></fragment>
  </body>
</html>")

(def template-1 "<!doctype html>
<html>
  <head>
    <meta charset=\"utf-8\">
    <link href=\"data:image/x-icon;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQEAYAAABPYyMiAAAABmJLR0T///////8JWPfcAAAACXBIWXMAAABIAAAASABGyWs+AAAAF0lEQVRIx2NgGAWjYBSMglEwCkbBSAcACBAAAeaR9cIAAAAASUVORK5CYII=\" rel=\"icon\" type=\"image/x-icon\" />
    <script type=\"slot\" name=\"head\"></script>
    <script>
    define('word', function () {
                                // Example dependency for the fragments
                                return 'initialised';
                                });
    </script>
  </head>
  <body>
    <slot name=\"body-start\"></slot>
    <div>
      <script>
        // this tests that the pipe functionality may
        // not be broken by script after the placeholder scripts
        document.body.appendChild(document.createElement('script'));
      </script>
      <h2>Fragment 1:</h2>
      <fragment src=\"http://localhost:8088\" primary fallback-src=\"http://localhost:8081\"></fragment>
      <h2>Fragment 2:</h2>
      <fragment async src=\"http://localhost:8082\"></fragment>
      <div>All done!</div>
    </div>
  </body>
</html>")

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

(defrecord Fragment [src primary? async? fallback-src])

(defun attr-to-string
       ([{:name name :value nil}] name)
       ([{:name name :value value}] (str name "=\"" value "\"")))

(defn attrs-to-string [attrs]
  (->> attrs
       (map (fn [attr] (attr-to-string attr)))
       (str/join " ")))

(defn add-leading-space [s]
  (if (empty? s)
    s
    (str " " s)))

(def attrs [{:name "n1" :value "v1"} {:name "n3" :value nil} {:name "n2" :value "v2"}])
(attrs-to-string attrs)

(comment
  (parser "<hello>")
  (parser "<hello src=\"hello\"/>")
  (parser "<hello src=\"hello\">")
  (parser "<hello>world >>> xx bb</hello>")
  (parser template)
  (parser template-1)
  (let [nodes (tr/transform {:tags            (fun ([& rest] rest))
                             :close-tag       (fun
                                                ([[:name "fragment"]]
                                                  nil)
                                                ([[:name close-tag-name]]
                                                  {:text (str "</" close-tag-name ">")}))
                             :text            (fn [text] {:text text})
                             :attr            (fn [attr] attr)
                             :attr-with-value (fun ([[:name name] [:attr-value attr-value]] {:name  name
                                                                                             :value attr-value}))
                             :attr-no-value   (fun ([[:name name]] {:name  name
                                                                    :value nil}))
                             :tag             (fun ([[:name "slot"] & attrs] {:slot (into {} attrs)})
                                                   ([[:name "fragment"] & attrs] {:fragment attrs})
                                                   ([[:name tag-name] & attrs]
                                                     {:text (str "<" tag-name (add-leading-space
                                                                                (attrs-to-string attrs)) ">")}))
                             :closed-tag      (fun ([[:name "slot"] & attrs] {:slot (into {} attrs)})
                                                   ([[:name "fragment"] & attrs] {:fragment attrs})
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
