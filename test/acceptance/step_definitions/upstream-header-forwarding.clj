(use 'piper.core)                                           ;; yes, no namespace declaration
(use '[feature-utils :refer :all])
(use '[world :as world])

(Given #"^a piper app with a fragment which returns the x-headers it gets$" []
       (init-piper "templates/show-x-headers.html"))

(Given #"^I prepare the header with name \"([^\"]*)\" and value \"([^\"]*)\"$"
       [header-name header-value]

       (if-not ((world/value) :prepared-headers)
         (world/swap-world! assoc :prepared-headers {header-name header-value})
         (let [existing-prepared-headers
               (assoc ((world/value) :prepared-headers) header-name header-value)]
           (world/swap-world! assoc :prepared-headers
                              existing-prepared-headers))))

(Given #"^I prepare the header with name \"([^\"]*)\" and values \"([^\"]*)\" and \"([^\"]*)\"$"
       [header-name header-value-1 header-value-2]

       (if-not ((world/value) :prepared-headers)
         (world/swap-world! assoc :prepared-headers {header-name [header-value-1 header-value-2]})
         (let [existing-prepared-headers
               (assoc ((world/value) :prepared-headers) header-name [header-value-1 header-value-2])]
           (world/swap-world! assoc :prepared-headers
                              existing-prepared-headers))))