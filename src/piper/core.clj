(ns piper.core
  (:require
    [piper.fragments :as fragments])
  (:gen-class))

(defn -main
  "Start the fragments"
  [& args]
  (println "Starting the fragments")
  (fragments/start-fragments))
