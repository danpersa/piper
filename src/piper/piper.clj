(ns piper.piper
  (:require
    [piper.template :as tp]
    [clojure.core.async :as async]
    [immutant.web.async :as iasync]
    [immutant.web :as web]
    [piper.client :as cl]
    [piper.chans :as ch]
    [piper.files :as fs]))

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

(comment
  (let [template (fs/classpath-file-as-str "template-1.html")]
    (web/run (piper-app template)
             :host "localhost" :port 8081 :path (str "/template-1")))


  (let [template (fs/classpath-file-as-str "template-2.html")]
    (web/run (piper-app template)
             :host "localhost" :port 8081 :path (str "/template-2")))

  (let [template (fs/classpath-file-as-str "primary-500.html")]
    (web/run (piper-app template)
             :host "localhost" :port 8081 :path (str "/template-500"))))
