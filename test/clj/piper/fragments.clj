(ns piper.fragments
  (:require
    [immutant.web :as web]
    [immutant.web.async :as iasync]
    [clojure.data.json :as json]
    [clojure.string :as str]))

(def ^:private localhost "localhost")

(def ^:private bloat (str (range 0 5)))

(defn- async-fragment [name num]
  (fn [request]
    (iasync/as-channel request
                       {:on-open (fn [stream]
                                   (do
                                     (iasync/send! stream (str name "\n"))
                                     (dotimes [msg num]
                                       (iasync/send! stream (str name " " msg " " bloat "\n")
                                                     {:close? (= msg (- num 1))})
                                       (Thread/sleep 70))))})))

(defn- run-async-fragment [name num]
  (web/run (async-fragment name num) :host localhost :port 8083 :path (str "/" name)))

(defn- fragment [name]
  (fn [request]
    {:status 200
     :body   (str "Hello world and " name "\n")}))

(defn- error-fragment [request]
  {:status 500
   :body   "Hello world 500\n"})

(defn- error-sleep-fragment [request]
  (Thread/sleep 1000)
  (error-fragment request))

(defn- sleep-fragment [request]
  (Thread/sleep 1000)
  {:status 200
   :body   (str "Hello world and sleep\n")})

(defn- headers-fragment [request]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (->> (seq (:headers request))
                 (filter (fn [[key _]] (.startsWith key "x-")))
                 (map (fn [[key value]] (str "<div>" key ": " value "</div>")))
                 (str/join "\n"))})

(defn- run-fragment [name]
  (web/run (fragment name) :host localhost :port 8083 :path (str "/" name)))

(defn start-fragments []
  (web/run error-fragment :host localhost :port 8083 :path (str "/error"))
  (web/run error-sleep-fragment :host localhost :port 8083 :path (str "/error-sleep"))
  (web/run sleep-fragment :host localhost :port 8083 :path (str "/sleep"))
  (web/run headers-fragment :host localhost :port 8083 :path (str "/headers"))
  (run-async-fragment "async-fragment-1" 10)
  (run-async-fragment "async-fragment-2" 15)
  (run-async-fragment "async-fragment-3" 20)

  (run-fragment "fragment-1")
  (run-fragment "fragment-2")
  (run-fragment "fragment-3"))

(comment
  (start-fragments))
