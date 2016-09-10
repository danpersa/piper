(use 'piper.core)                                           ;; yes, no namespace declaration
(use '[feature-utils :refer :all])
(use '[world :as world])

(Given #"^a piper app with a fragment which returns the x-headers it gets$" []
       (init-piper "templates/show-x-headers.html"))

(When #"^I do a request to the piper app with the header name \"([^\"]*)\" and value \"([^\"]*)\"$"
      [header-name header-value]

      (let [response (http/sync-get "http://localhost:8081/piper"
                                    :headers {header-name header-value})]
        (world/reset-world! {:response response})))