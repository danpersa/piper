(ns piper.piper
  (:require
    [piper.template :as tp]
    [clojure.core.async :as async]
    [http.async.client.request :as req]
    [immutant.web.async :as iasync]
    [immutant.web :as web]
    [piper.client :as cl]
    [piper.chans :as ch]
    [piper.files :as fs]))


(defn small-piper [request]
  (let [request1 (req/prepare-request :get "http://localhost:8083/fragment-1")
        out-chan (async/chan 4096)]



    (iasync/as-channel request
                       {:on-open (fn [stream]
                                   (async/go (req/execute-request cl/client request1
                                                                  :part (cl/body-collect out-chan)
                                                                  :completed (cl/body-completed out-chan)))

                                   (loop [current-chunk (async/<!! out-chan)]
                                     (if-some [next-chunk (async/<!! out-chan)]
                                       (do
                                         (iasync/send! stream current-chunk)
                                         (recur next-chunk))
                                       (iasync/send! stream current-chunk {:close? true}))))})))

(defn piper-concat-app [urls]
  (fn [request]
    (let [out-chan (async/chan 4096)]

      (iasync/as-channel request
                         {:on-open (fn [stream]
                                     (let [in-chans (for [url urls]
                                                      ((cl/async-request url) :body-chan))]

                                       (ch/concat-chans in-chans out-chan)
                                       (loop [chunk (async/<!! out-chan)]
                                         (if-some [next-chunk (async/<!! out-chan)]
                                           (do
                                             (iasync/send! stream chunk)
                                             (recur next-chunk))
                                           (iasync/send! stream chunk {:close? true})))))}))))

(defn piper-app [template]
  (let [parsed-template (tp/parse-template template)
        primary-fragments (tp/select-primary (tp/fragment-nodes parsed-template))]


    (fn [request]
      (if-some [fragment-chans (cl/call-fragments primary-fragments)]
        (let [out-chan (async/chan 4096)]
          (iasync/as-channel request
                             {:on-open (fn [stream]
                                         (let [ast-chans (ch/ast-to-channels parsed-template fragment-chans)]

                                           (ch/concat-chans ast-chans out-chan)
                                           (loop [chunk (async/<!! out-chan)]
                                             (if-some [next-chunk (async/<!! out-chan)]
                                               (do
                                                 (iasync/send! stream chunk)
                                                 (recur next-chunk))
                                               (iasync/send! stream chunk {:close? true})))))}))
        {:status 500
         :body   "There was a timeout or 500 from primary"}))))

(defn sync-piper []
  (piper-concat-app ["http://localhost:8083/fragment-1"
                     "http://localhost:8083/fragment-2"
                     "http://localhost:8083/fragment-3"]))

(defn async-piper []
  (piper-concat-app ["http://localhost:8083/async-fragment-1"
                     "http://localhost:8083/async-fragment-2"
                     "http://localhost:8083/async-fragment-3"
                     "http://localhost:8083/async-fragment-1"]))

(defn mixt-piper []
  (piper-concat-app ["http://localhost:8083/fragment-1"
                     "http://localhost:8083/async-fragment-2"
                     "http://localhost:8083/fragment-3"]))

(web/run (async-piper) :host "localhost" :port 8081 :path (str "/async-piper"))
(web/run small-piper :host "localhost" :port 8081 :path (str "/small-piper"))
(web/run (sync-piper) :host "localhost" :port 8081 :path (str "/piper"))
(web/run (mixt-piper) :host "localhost" :port 8081 :path (str "/mixt-piper"))

(let [template (fs/classpath-file-as-str "template-1.html")]
  (web/run (piper-app template)
           :host "localhost" :port 8081 :path (str "/template-1")))


(let [template (fs/classpath-file-as-str "template-2.html")]
  (web/run (piper-app template)
           :host "localhost" :port 8081 :path (str "/template-2")))

(let [template (fs/classpath-file-as-str "primary-500.html")]
  (web/run (piper-app template)
           :host "localhost" :port 8081 :path (str "/template-500")))