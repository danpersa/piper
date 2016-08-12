(ns piper.client
  (:require [http.async.client :as ac]
            [clojure.core.async :as async]
            [http.async.client.request :as req])
  (:import (java.io ByteArrayOutputStream)))


(def client (ac/create-client))

(defn- convert [#^ByteArrayOutputStream baos enc]
  (.toString baos #^String enc))

(defn body-collect [hi-chan]

  (fn [state baos]
    ;(println "Got a part " baos)
    (async/>!! hi-chan (convert baos "UTF-8"))
    [baos :continue]))

(defn body-completed [hi-chan]

  (fn [_]
    ;(println "Body completed")
    (async/close! hi-chan)
    [true :continue]))

(defn async-request [url]
  (let [hi-chan (async/chan 1024)]
    (async/go (let [request (req/prepare-request :get url)]
                (req/execute-request client request
                                     :part (body-collect hi-chan)
                                     :completed (body-completed hi-chan))))
    hi-chan))

(defn call-fragments
  "Calls the primary fragment and the other fragments.
   In case of a 200 from the primary fragment, we return a list of channels."
  [{:keys [primary fragments]}]
  (let [primary-channel (async-request (:src primary))
        fragment-channels (into {}
                                (map (fn [fragment]
                                       {(:id fragment) (async-request (:src fragment))}) fragments))]
    (assoc fragment-channels (:id primary) primary-channel)))
