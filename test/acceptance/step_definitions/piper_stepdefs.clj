(use 'piper.core)                                           ;; yes, no namespace declaration
(use '[piper.piper :as piper])
(use '[piper.files :as fs])
(use '[clj-http.client :as client])
(use '[midje.sweet :refer :all])
(use '[immutant.web :as web])
(use '[clojure.string :as str])

(def world (atom {:result ""}))

(defn init-piper [template-path]
  (-> template-path
      fs/classpath-file-as-str
      piper/piper-app
      (web/run :host "localhost" :port 8081 :path (str "/piper"))))

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
      (let [response (client/get "http://localhost:8081/piper" {:throw-exceptions false})]
        (reset! world {:result (str (response :body))})))

(Then #"^I should get the correct html page$" []
      (fact
        (@world :result) =>
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
                   "</html>"])))

(Then #"^I should get an error$" []
      (fact
        (@world :result) => "There was a timeout or 500 from primary"))

(Then #"^the timed out fragment content should not be included$" []
      (fact
        (@world :result) =>
        (str/join "\n"
                  ["<html>"
                   "<body>"
                   "<div>Hello</div>"
                   ""
                   "<div>Hello1</div>"
                   "Hello world and fragment-1"
                   ""
                   "</body>"
                   "</html>"])))