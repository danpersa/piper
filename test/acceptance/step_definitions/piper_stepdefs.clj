(use 'piper.core)                                           ;; yes, no namespace declaration
(use '[piper.piper :as piper])
(use '[piper.fragments :as fragments])
(use '[piper.files :as fs])
(use '[clj-http.client :as client])
(use '[midje.sweet :refer :all])
(use '[immutant.web :as web])

(def world (atom {:result ""}))

(Given #"^a default piper app$" []
       (-> "templates/simple-template.html"
           fs/classpath-file-as-str
           piper/piper-app
           (web/run :host "localhost" :port 8081 :path (str "/simple-template"))))

(When #"^I do a request for the default template$" []
      (let [response (client/get "http://localhost:8081/simple-template")]
        (reset! world {:result (str (response :body))})))

(Then #"^I should get the correct html page$" []
      (fact
        (@world :result) => "<html>\n<body>\n<div>Hello</div>\nHello world and fragment-1\n\nXXXX\nHello world and fragment-2\n\n</body>\n</html>"))
