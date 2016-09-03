(ns piper.client
  (:require [clojure.core.async :as async :refer [>!! <!! >! go]]
            [clojure.tools.logging :as log]
            [defun :refer :all]
            [core.async.http.client :as http]))

(defn async-request [url & {:keys [timeout]}]
  (let [status-chan (async/chan 1)
        headers-chan (async/chan 1)
        body-chan (async/chan 1024)
        ; TODO handle the error
        error-chan (async/chan 1)]
    (log/info "Start request to url:" url)
    (http/get url
              :status-chan status-chan
              :headers-chan headers-chan
              :body-chan body-chan
              :error-chan error-chan
              ; TODO make default timeout configurable
              :timeout (or timeout 3000))

    {:status-chan status-chan :headers-chan headers-chan :body-chan body-chan}))

(defn- convert-to-int [s]
  (if (nil? s)
    nil
    (Integer. s)))

(defn- fragments->id-to-chan
  "Gets a list of fragments. Calls the fragments.
   Returns a map from the fragment id to the go chan where the body of the fragment will get into"
  [fragments]
  (into {}
        (map (fn [fragment]
               {(:id fragment)
                (:body-chan (async-request (:src fragment)
                                           :timeout (convert-to-int (:timeout fragment))))})
             fragments)))

(defun call-fragments
       "Calls the primary fragment and the other fragments.
        In case of a 200 from the primary fragment, we return a list of channels."
       ([({:fragments fragments} :only [:fragments])]
         (let [fragment-channels (fragments->id-to-chan fragments)]
           fragment-channels))
       ([{:primary primary :fragments fragments}]

         (log/error "primary timeout: " primary " " (:timeout primary))

         ; TODO handle headers
         (let [{:keys [status-chan headers-chan body-chan]}
               (async-request (:src primary)
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
  (http/sync-get "http://localhost:8083/fragment-1")
  (async-request "http://localhost:8083/async-fragment-1")
  (call-fragments {:primary   {:id 1 :src "http://localhost:8083/async-fragment-1"}
                   :fragments [{:id 2 :src "http://localhost:8083/fragment-1"}]})
  (call-fragments {:primary   {:id 1 :src "http://localhost:8083/error"}
                   :fragments [{:id 2 :src "http://localhost:8083/fragment-1"}]})

  (call-fragments {:primary   {:id 1 :src "http://localhost:8083/error-sleep"}
                   :fragments [{:id 2 :src "http://localhost:8083/fragment-1"}]}))
