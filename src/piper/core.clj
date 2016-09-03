(ns piper.core
  (:require [piper.fragments :as fragments])
  (:gen-class))

(defn -main
  "Start the piper"
  [& args]

  (fragments/start-fragments))
