(ns sisyphus.config
  (:require [environ.core :refer [env]]))

(def defaults
  ^:displace {:http {:port 3000}
              :repositories 
              {
               :prd {
                     :url  "http://git@192.168.99.100:10080/root/app1-config.git" 
                     :branch "master"
                     :dir "app1-prd"}
               :stg {:url  
                     "ssh://git@localhost:10022/oms/app1-config.git" 
                     :branch "stg"
                     :dir "app1-stg"}
               :dev {:url "ssh://git@localhost:10022/oms/app1-config.git" 
                     :branch "dev"
                     :dir "app1-dev"} 
               :loc {:url "ssh://git@localhost:10022/oms/app1-config.git" 
                     :branch "loc"
                     :dir "app1-loc"}}
              :tmp-dir "resources/tmp"})

(def environ
  {:http {:port (some-> env :port Integer.)}})
