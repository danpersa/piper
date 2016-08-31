(ns piper.chans
  (:require [clojure.core.async :as async]
            [clojure.core.match :refer [match]]
            [piper.template :as tp]
            [piper.client :as cl]
            [piper.files :as fs]))

(defn concat-chans [in-chans out-chan]
  (async/go
    (loop [chans in-chans]
      (if-let [in-chan (first chans)]
        (if-some [chunk (async/<! in-chan)]
          (do
            (async/>! out-chan chunk)
            (recur chans))
          (recur (rest chans)))))
    (async/close! out-chan)))

(defn ast-to-channels [ast fragment-chans]
  (loop [nodes ast
         result []]
    (if-some [node (first nodes)]
      (let [new-chan (match node
                            {:text text}
                            (let [chan (async/chan 1)]
                              ; (println "Matched text")
                              (async/>!! chan text)
                              (async/close! chan)
                              chan)
                            {:fragment {:id id}}
                            (let [chan (async/chan 1)]
                              ;(println "Matched fragment " id)
                              (fragment-chans id))
                            :else
                            (let [chan (async/chan 1)]
                              ;(println "Matched else ")
                              ; TODO match other
                              (async/>!! chan "XXXX")
                              (async/close! chan)
                              chan))]
        ; (println "We recur node " node)
        (recur (rest nodes) (conj result new-chan)))
      result)))

(defn render-channels [chans]
  (let [out-chan (async/chan 10)]
    (concat-chans chans out-chan)
    (loop [chunk (async/<!! out-chan)]
      (when (some? chunk)
        (do (println chunk)
            (recur (async/<!! out-chan)))))))


(comment
  (let [template (fs/classpath-file-as-str "template-1.html")
        parsed-template (tp/parse-template template)
        primary-fragments (tp/select-primary (tp/fragment-nodes parsed-template))
        fragment-chans (cl/call-fragments primary-fragments)
        render-channels (render-channels (ast-to-channels parsed-template fragment-chans))]
    fragment-chans))
