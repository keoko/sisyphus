(ns sisyphus.endpoint.config
  (:require [compojure.core :refer :all]
            [sisyphus.schema :refer [build-schema validate-schema]]
            [sisyphus.data-store :refer [load-data]]))


(defn config-endpoint
  [conf]
  (context "/config" []
           (GET ["/:env/:config-key" :config-key #".*"] 
                [env :<< keyword 
                 config-key :<< str]
                (let [config (load-data config-key env)
                      schema (build-schema)]
                  (try                    
                    (if (or true config (validate-schema schema config))
                      {:status 200
                       :headers {"Content-Type" "text/html; charset=utf-8"}
                       :body (str config)}
             
                      {:status 404
                       :headers {"Content-Type" "text/html; charset=utf-8"}
                       :body (str  "not found or invalid.")})
                    (catch Exception e 
                      {:status 500
                       :headers {"Content-Type" "text/html; charset=utf-8"}
                       :body (.getMessage e)}))))))
