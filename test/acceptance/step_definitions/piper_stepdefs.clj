(use 'piper.core)                                           ;; yes, no namespace declaration
(use '[clojure.string :as str])
(use '[core.async.http.client :as http])
(use '[feature-utils :refer :all])
(use '[world :as world])

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
      (let [response (http/sync-get "http://localhost:8081/piper")]
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
        (assert
          (= expected-result
             (world/response-body)))))

(Then #"^I should get an error$" []
      (assert
        (=
          "There was a timeout or 500 from primary"
          (world/response-body))))

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
        (assert
          (= expected-result
             (world/response-body)))))

(Then #"^I should get the body \"([^\"]*)\"$" [expected-body]
      (assert (= expected-body (world/response-body))))
