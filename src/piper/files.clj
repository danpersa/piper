(ns piper.files
  (:require [clojure.java.io :as io]))


(defn classpath-file-as-str [name]
  (let [file (io/file
               (io/resource
                 name))]
    (slurp file)))

(comment
  (classpath-file-as-str "template-1.html"))
