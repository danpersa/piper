(use 'piper.core)                                           ;; yes, no namespace declaration
(use '[feature-utils :refer :all])

(def world (atom {:result ""}))

(Given #"^a piper app with a fragment which returns the x-headers it gets$" []
       (init-piper "templates/show-x-headers.html"))

(When #"^I do a request to the piper app with the header name \"([^\"]*)\" and value \"([^\"]*)\"$"
      [header-name header-value]

      (let [response (http/sync-get "http://localhost:8081/piper"
                                    :headers {header-name header-value})]
        (reset! world {:result (str (response :body))})))

(Then #"^I should get the \"([^\"]*)\" header as a response$" [arg1]
      (println "Result: " (@world :result)))