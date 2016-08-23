(use 'piper.core)                                           ;; yes, no namespace declaration
(use '[piper.fragments :as fragments])
(use 'clojure.test)
(use '[http.async.client :as http])
(use '[http.async.client.request :as req])

;(def client (http/create-client))

(def world (atom {:result ""}))

(Given #"^Some fragments$" []
       (fragments/start-fragments)
       (Thread/sleep 1000))

(When #"^I check if the fragments are started$" []

      (with-open [client (http/create-client)]


        (let [request (req/prepare-request :get "http://www.webopedia.com/")
              response (http/await (req/execute-request client request))]
          (Thread/sleep 1000)


          (println (str response))
          ;(println result)
          )))

(Then #"^they should be started$" []
      (comment Write code here that turns the phrase above into concrete actions)
      (throw (cucumber.api.PendingException.)))
