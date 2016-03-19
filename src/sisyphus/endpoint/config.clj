(ns sisyphus.endpoint.config
  (:require [compojure.core :refer :all]
            [sisyphus.schema :refer [build-schema validate-schema]]
            [sisyphus.data-store :refer [load-data]]
            [taoensso.timbre :as timbre
             :refer (info)]
            [taoensso.timbre.appenders.core :as appenders]))

(timbre/set-level! :debug)


(defn log-output-format
  ([data] 
   (log-output-format nil data))
  ([_ {:keys [timestamp_ msg_]}]
   (str @timestamp_ ": " (force  msg_))))

(defn add-logger
  []
  (timbre/merge-config!
   {:appenders {:spit (appenders/spit-appender {:fname "/tmp/my-file.log"})}
    :output-fn log-output-format}))


(defn config-endpoint
  [conf]
  (context "/config" []
           (GET ["/:env/:config-key" :config-key #".*"] 
                [env :<< keyword 
                 config-key :<< str]
                (let [config (load-data config-key env)
                      schema (build-schema)]
                  (add-logger)
                  (info "endpoint config request")
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
