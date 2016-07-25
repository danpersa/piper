(ns piper.fragments
  (:require
    [immutant.web :as web]
    [immutant.web.async :as iasync]))

(def bloat (str (range 0 5)))

(defn- async-fragment [name num]
  (fn [request]
    (iasync/as-channel request
                       {:on-open (fn [stream]
                                   (do
                                     (iasync/send! stream (str name "\n"))
                                     (dotimes [msg num]
                                       (iasync/send! stream (str name " " msg " " bloat "\n")
                                                     {:close? (= msg (- num 1))})
                                       (Thread/sleep 70)
                                       )))})))

(defn- run-async-fragment [name num]
  (web/run (async-fragment name num) :host "localhost" :port 8083 :path (str "/" name)))

(defn- fragment [name]
  (fn [request]
    {:status 200
     :body   (str "Hello world and " name "\n")}))

(defn- run-fragment [name]
  (web/run (fragment name) :host "localhost" :port 8083 :path (str "/" name)))

(defn start-fragments []
  (run-async-fragment "async-fragment-1" 10)
  (run-async-fragment "async-fragment-2" 15)
  (run-async-fragment "async-fragment-3" 20)

  (run-fragment "fragment-1")
  (run-fragment "fragment-2")
  (run-fragment "fragment-3"))

