(use 'piper.core)                                           ;; yes, no namespace declaration
(use '[piper.piper :as piper])
(use '[piper.files :as fs])
(use '[clj-http.client :as client])
(use '[midje.sweet :refer :all])
(use '[immutant.web :as web])

(def world (atom {:result ""}))

(defn init-piper [template-path]
  (-> template-path
      fs/classpath-file-as-str
      piper/piper-app
      (web/run :host "localhost" :port 8081 :path (str "/piper"))))

(Given #"^a default piper app$" []
       (init-piper "templates/simple-template.html"))

(When #"^I do a request to the piper app$" []
      (let [response (client/get "http://localhost:8081/piper" {:throw-exceptions false})]
        (reset! world {:result (str (response :body))})))

(Then #"^I should get the correct html page$" []
      (fact
        (@world :result) => "<html>\n<body>\n<div>Hello</div>\nHello world and fragment-1\n\nXXXX\nHello world and fragment-2\n\n</body>\n</html>"))


(Given #"^a piper app with a primary fragment which returns (\d+)$" [arg1]
       (init-piper "templates/primary-500.html"))

(Then #"^I should get an error$" []
      (fact
        (@world :result) => "There was a 500 from primary"))
