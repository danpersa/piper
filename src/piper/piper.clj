(ns piper.piper
  (:require
    [clj-http.client :as client]
    [clojure.core.async :as async]
    [clojure.java.io :as io]
    [clojure.string :as str]
    [qbits.jet.client.http :as http]
    [http.async.client :as ac]
    [http.async.client.request :as req]
    [immutant.web.async :as iasync]
    [immutant.web :as web])
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

(defn small-piper [request]
  (let [request1 (req/prepare-request :get "http://localhost:8083/fragment-1")
        out-chan (async/chan 4096)]



    (iasync/as-channel request
                       {:on-open (fn [stream]
                                   (async/go (req/execute-request client request1
                                                                  :part (body-collect out-chan)
                                                                  :completed (body-completed out-chan)))

                                   (loop [current-chunk (async/<!! out-chan)]
                                     (if-some [next-chunk (async/<!! out-chan)]
                                       (do
                                         (iasync/send! stream current-chunk)
                                         (recur next-chunk))
                                       (iasync/send! stream current-chunk {:close? true}))))})))

(defn async-request [url]
  (let [hi-chan (async/chan 1024)]
    (async/go (let [request (req/prepare-request :get url)]
                (req/execute-request client request
                                     :part (body-collect hi-chan)
                                     :completed (body-completed hi-chan))))
    hi-chan))

(defn piper-app [urls]
  (fn [request]
    (let [out-chan (async/chan 4096)]

      (iasync/as-channel request
                         {:on-open (fn [stream]
                                     (let [in-chans (for [url urls]
                                                      (async-request url))]

                                       (concat-chans in-chans out-chan)
                                       (loop [chunk (async/<!! out-chan)]
                                         (if-some [next-chunk (async/<!! out-chan)]
                                           (do
                                             (iasync/send! stream chunk)
                                             (recur next-chunk))
                                           (iasync/send! stream chunk {:close? true})))))}))))

(defn sync-piper []
  (piper-app ["http://localhost:8083/fragment-1"
              "http://localhost:8083/fragment-2"
              "http://localhost:8083/fragment-3"
              "http://localhost:8083/fragment-1"
              "http://localhost:8083/fragment-2"
              "http://localhost:8083/fragment-3"
              ]))

(defn async-piper []
  (piper-app ["http://localhost:8083/async-fragment-1"
              "http://localhost:8083/async-fragment-2"
              "http://localhost:8083/async-fragment-3"
              "http://localhost:8083/async-fragment-1"]))

(defn mixt-piper []
  (piper-app ["http://localhost:8083/fragment-1"
              "http://localhost:8083/async-fragment-2"
              "http://localhost:8083/fragment-3"]))

(web/run (async-piper) :host "localhost" :port 8081 :path (str "/async-piper"))
(web/run small-piper :host "localhost" :port 8081 :path (str "/small-piper"))
(web/run (sync-piper) :host "localhost" :port 8081 :path (str "/piper"))
(web/run (mixt-piper) :host "localhost" :port 8081 :path (str "/mixt-piper"))























