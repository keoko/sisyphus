(ns sisyphus.config
  (:require [environ.core :refer [env]]))

(def defaults
  {:http {:port 3000}
   :scheduler {}
   :repositories 
   {:app1-prd {
               :url  "ssh://git@192.168.99.100:10022/root/app1-prd.git" 
               :env :app1-prd
               :branch "master"
               :dir "/tmp/app1-prd"
               }
    :app1-stg {
               :url  "http://git@192.168.99.100:10080/root/app2.git" 
               :branch "stg"
               :dir "/tmp/app1-stg"
               :env :app1-stg
               }
    }
   })


(def environ
  {:http {:port (some-> env :port Integer.)}})
