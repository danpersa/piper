(use 'piper.core)                                           ;; yes, no namespace declaration
(use '[piper.fragments :as fragments])
(use '[core.async.http.client :as http])
(use '[world :as world])

(Given #"^some fragments$" []
       (fragments/start-fragments))

(When #"^I check if the fragments are started$" []
      (let [response (http/sync-get "http://localhost:8083/fragment-1")]
        (world/reset-world! {:result (str (response :body))})))

(Then #"^they should be started$" []
      (assert (= "Hello world and fragment-1\n"
                 ((world/value) :result))))
