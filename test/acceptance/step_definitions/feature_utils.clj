(ns feature-utils
  (:require [immutant.web :as web]
            [piper.piper :as piper]
            [piper.files :as fs]))

(defn init-piper [template-path]
  (-> template-path
      fs/classpath-file-as-str
      piper/piper-app
      (web/run :host "localhost" :port 8081 :path (str "/piper"))))
