(ns sisyphus.endpoint.group
  (:require [compojure.core :refer :all]))

(defn group-endpoint
  [conf]
  (context "/group" []
           (GET "/" [] 
                {:headers {"Access-Control-Allow-Origin" "*"
                           "Access-Control-Allow-Methods" "GET,PUT,POST,DELETE,OPTIONS"
                           "Access-Control-Allow-Headers" "X-Requested-With,Content-Type,Cache-Control"}
                 :body {:content "Hello World"
                        :foo {:bar "quux"}}})))
