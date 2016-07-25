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

                                   (loop []
                                     (if-some [chunk (async/<!! out-chan)]
                                       (do
                                         (iasync/send! stream chunk)
                                         (recur))
                                       (iasync/send! stream "" {:close? true}))))})))


(defn three-channel-app [url1 url2 url3]
  (fn [request]
    (let [request1 (req/prepare-request :get url1)
          request2 (req/prepare-request :get url2)
          request3 (req/prepare-request :get url3)
          hi-chan1 (async/chan 1024)
          hi-chan2 (async/chan 1024)
          hi-chan3 (async/chan 1024)
          in-chans [hi-chan1 hi-chan2 hi-chan3]
          out-chan (async/chan 4096)]

      (iasync/as-channel request
                         {:on-open (fn [stream]
                                     (async/go (req/execute-request client request1
                                                                    :part (body-collect hi-chan1)
                                                                    :completed (body-completed hi-chan1)))


                                     (async/go (req/execute-request client request2
                                                                    :part (body-collect hi-chan2)
                                                                    :completed (body-completed hi-chan2)))

                                     (async/go (req/execute-request client request3
                                                                    :part (body-collect hi-chan3)
                                                                    :completed (body-completed hi-chan3)))

                                     (concat-chans in-chans out-chan)

                                     (loop []
                                       (if-some [chunk (async/<!! out-chan)]
                                         (do
                                           (iasync/send! stream chunk)
                                           (recur))
                                         (iasync/send! stream "" {:close? true}))))}))))

(defn sync-piper []
  (three-channel-app "http://localhost:8083/fragment-1"
                     "http://localhost:8083/fragment-2"
                     "http://localhost:8083/fragment-3"))

(defn async-piper []
  (three-channel-app "http://localhost:8083/async-fragment-1"
                     "http://localhost:8083/async-fragment-2"
                     "http://localhost:8083/async-fragment-3"))

(defn mixt-piper []
  (three-channel-app "http://localhost:8083/fragment-1"
                     "http://localhost:8083/async-fragment-2"
                     "http://localhost:8083/fragment-3"))

(web/run (async-piper) :host "localhost" :port 8081 :path (str "/async-piper"))
(web/run small-piper :host "localhost" :port 8081 :path (str "/small-piper"))
(web/run (sync-piper) :host "localhost" :port 8081 :path (str "/piper"))
(web/run (mixt-piper) :host "localhost" :port 8081 :path (str "/mixt-piper"))























