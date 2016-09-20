(use 'piper.core)                                           ;; yes, no namespace declaration
(use '[clojure.string :as str])
(use '[core.async.http.client :as http])
(use '[feature-utils :refer :all])
(use '[world :as world])
(use '[speclj.core :refer :all])

(Given #"^a default piper app$" []
       (init-piper "templates/simple-template.html"))

(Given #"^a piper app without a primary fragment$" []
       (init-piper "templates/no-primary.html"))

(Given #"^a piper app with a primary fragment which returns (\d+)$" [arg1]
       (init-piper "templates/primary-500.html"))

(Given #"^a piper app with a primary fragment which returns a timeout$" []
       (init-piper "templates/primary-timeout.html"))

(Given #"^a piper app with a fragment which returns a timeout$" []
       (init-piper "templates/fragment-timeout.html"))

(When #"^I do a request to the piper app$" []
      (let [prepared-headers ((world/value) :prepared-headers)
            response (http/sync-get "http://localhost:8081/piper"
                                    :headers prepared-headers)]
        (world/reset-world! {:response response})))

(Then #"^I should get the correct html page$" []

      (let [expected-result
            (str/join "\n"
                      ["<html>"
                       "<body>"
                       "<div>Hello</div>"
                       "Hello world and fragment-1"
                       ""
                       "<div>Hello1</div>"
                       "Hello world and fragment-2"
                       ""
                       "</body>"
                       "</html>"])]
        (should= expected-result
                 (world/response-body))))

(Then #"^I should get an error$" []
      (should= "There was a timeout or 500 from primary"
               (world/response-body)))

(Then #"^the timed out fragment content should not be included$" []
      (let [expected-result
            (str/join "\n"
                      ["<html>"
                       "<body>"
                       "<div>Hello</div>"
                       ""
                       "<div>Hello1</div>"
                       "Hello world and fragment-1"
                       ""
                       "</body>"
                       "</html>"])]
        (should= expected-result (world/response-body))))

(Then #"^I should get the body \"([^\"]*)\"$" [expected-body]
      (should= expected-body (world/response-body)))
