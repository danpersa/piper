(ns piper.client
  (:require [http.async.client :as ac]
            [clojure.core.async :as async :refer [>!! <!! >! go]]
            [http.async.client.request :as req]
            [clojure.tools.logging :as log]
            [defun :refer :all])
  (:import (java.io ByteArrayOutputStream)))


(def client (ac/create-client))

(defn- convert [#^ByteArrayOutputStream baos enc]
  (.toString baos #^String enc))

(defn body-collect [body-chan]

  (fn [state baos]
    ;(log/info "Got a part " baos)
    (async/>!! body-chan (convert baos "UTF-8"))
    [baos :continue]))

(defn error-collect [body-chan status-chan headers-chan]

  (fn [_ err]
    (log/error err "error completed")
    (async/close! status-chan)
    (async/close! headers-chan)
    (async/close! body-chan)
    err))

(defn body-completed [body-chan]

  (fn [_]
    (log/info "Body completed")
    (async/close! body-chan)
    [true :continue]))

(defn status-collect [status-chan]

  (fn [_ status]
    (log/info "Status  collect" (:code status))
    (>!! status-chan (:code status))
    [status :continue]))

(defn headers-collect [headers-chan]
  (fn [_ headers]
    (log/info "Headers collect" (:keys headers))
    (>!! headers-chan headers)
    [headers (if-not headers :abort)]))

(defn async-request [url & {:keys [timeout]}]
  (let [status-chan (async/chan 5)
        headers-chan (async/chan 5)
        body-chan (async/chan 1024)]
    (log/info "Start request to url:" url)
    (let [request (req/prepare-request :get url
                                       ;TODO make default timeout configurable
                                       :timeout (or timeout 2000))]
      (log/info "Start request")
      (req/execute-request client request
                           :status (status-collect status-chan)
                           :headers (headers-collect headers-chan)
                           :part (body-collect body-chan)
                           :completed (body-completed body-chan)
                           :error (error-collect body-chan status-chan headers-chan)))
    {:status-chan status-chan :headers-chan headers-chan :body-chan body-chan}))

(defn- fragments->id-to-chan
  "Gets a list of fragments. Calls the fragments.
   Returns a map from the fragment id to the go chan where the body of the fragment will get into"
  [fragments]
  (into {}
        (map (fn [fragment]
               {(:id fragment)
                (:body-chan (async-request (:src fragment)))})
             fragments)))

(defn- convert-to-int [s]
  (if (nil? s)
    nil
    (Integer. s)))

(defun call-fragments
       "Calls the primary fragment and the other fragments.
        In case of a 200 from the primary fragment, we return a list of channels."
       ([({:fragments fragments} :only [:fragments])]
         (let [fragment-channels (fragments->id-to-chan fragments)]
           fragment-channels))
       ([{:primary primary :fragments fragments}]

         (log/error "primary timeout: " primary " " (:timeout primary))

         (let [{:keys [status-chan headers-chan body-chan]} (async-request (:src primary)
                                                                           :timeout (convert-to-int (:timeout primary)))
               fragment-channels (fragments->id-to-chan fragments)]

           (let [primary-status (async/<!! status-chan)]
             (log/info "Primary status" primary-status)
             (if (= 200 primary-status)
               (assoc fragment-channels (:id primary) body-chan)
               (do
                 (log/error "Primary fragment returned 500")
                 nil))))))

(comment
  (async-request "http://localhost:8083/async-fragment-1")
  (call-fragments {:primary   {:id 1 :src "http://localhost:8083/async-fragment-1"}
                   :fragments [{:id 2 :src "http://localhost:8083/fragment-1"}]})
  (call-fragments {:primary   {:id 1 :src "http://localhost:8083/error"}
                   :fragments [{:id 2 :src "http://localhost:8083/fragment-1"}]})

  (call-fragments {:primary   {:id 1 :src "http://localhost:8083/error-sleep"}
                   :fragments [{:id 2 :src "http://localhost:8083/fragment-1"}]}))
