(ns sisyphus.config
  (:require [environ.core :refer [env]]))

(def defaults
  {:http {:port 3000}
   :scheduler {}
   :repositories 
   {
    :prd-oms-config {
                 :url  "ssh://git@192.168.99.100:10022/root/oms-config.git" 
                 :branch "pull-mode"
                 :dir "/tmp/prd-oms-config"
                 :env :prd-oms-config
                 }

    :stg-oms-config {
                     :url  "ssh://git@192.168.99.100:10022/root/oms-config.git" 
                     :env :stg-oms-config
                     :branch "stg"
                     :dir "/tmp/stg-oms-config"
                     }
    }
   })


(def environ
  {:http {:port (some-> env :port Integer.)}})
