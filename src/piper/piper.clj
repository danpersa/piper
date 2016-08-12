(ns piper.piper
  (:require
    [piper.template :as tp]
    [clojure.core.async :as async]
    [http.async.client.request :as req]
    [immutant.web.async :as iasync]
    [immutant.web :as web]
    [piper.client :as cl]
    [piper.chans :as ch]))


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
                                                      (cl/async-request url))]

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
      (let [out-chan (async/chan 4096)]

        (iasync/as-channel request
                           {:on-open (fn [stream]
                                       (let [fragment-chans (cl/call-fragments primary-fragments)
                                             ast-chans (ch/ast-to-channels parsed-template fragment-chans)]

                                         (ch/concat-chans ast-chans out-chan)
                                         (loop [chunk (async/<!! out-chan)]
                                           (if-some [next-chunk (async/<!! out-chan)]
                                             (do
                                               (iasync/send! stream chunk)
                                               (recur next-chunk))
                                             (iasync/send! stream chunk {:close? true})))))})))))

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

(let [template "<html>
  <head>
    <script type=\"fragment\" src=\"http://assets.domain.com\" attr></script>
  </head>
  <body>
    <slot name=\"body-start\"></slot>
    <fragment src=\"http://localhost:8083/fragment-1\"></fragment>
    <fragment src=\"http://localhost:8083/fragment-2\" primary></fragment>
    <fragment src=\"http://localhost:8083/fragment-3\" async></fragment>
    <fragment src=\"http://localhost:8083/fragment-1\" async></fragment>
  </body>
</html>"] (web/run (piper-app template)
                   :host "localhost" :port 8081 :path (str "/template-0")))


(let [template "<!doctype html>
<html>
  <head>
    <meta charset=\"utf-8\">
    <link href=\"data:image/x-icon;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQEAYAAABPYyMiAAAABmJLR0T///////8JWPfcAAAACXBIWXMAAABIAAAASABGyWs+AAAAF0lEQVRIx2NgGAWjYBSMglEwCkbBSAcACBAAAeaR9cIAAAAASUVORK5CYII=\" rel=\"icon\" type=\"image/x-icon\" />
    <script type=\"slot\" name=\"head\"></script>
    <script>
    define('word', function () {
                                // Example dependency for the fragments
                                return 'initialised';
                                });
    </script>
  </head>
  <body>
    <slot name=\"body-start\"></slot>
    <div>
      <script>
        // this tests that the pipe functionality may
        // not be broken by script after the placeholder scripts
        document.body.appendChild(document.createElement('script'));
      </script>
      <h2>Fragment 1:</h2>
      <fragment src=\"http://localhost:8083/fragment-1\" primary fallback-src=\"http://localhost:8081\"></fragment>
      <h2>Fragment 2:</h2>
      <fragment async src=\"http://localhost:8083/fragment-2\"></fragment>
      <div>All done!</div>
    </div>
  </body>
</html>"] (web/run (piper-app template)
                   :host "localhost" :port 8081 :path (str "/template-1")))