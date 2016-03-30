(ns sisyphus.config
  (:require [environ.core :refer [env]]))

(def defaults
  {:http {:port 3000}
   :scheduler {}
   :repositories 
   [{
     :url  "http://git@192.168.99.100:10080/root/app2.git" 
     :env :prd
     :branch "master"
     :dir "/tmp/app1-prd"
     }
    {
     :url  "http://git@192.168.99.100:10080/root/app2.git" 
     :branch "stg"
     :dir "/tmp/app1-stg"
     :env :stg}
]})


(def environ
  {:http {:port (some-> env :port Integer.)}})
