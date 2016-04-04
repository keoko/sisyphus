(ns sisyphus.endpoint.config
  (:require [compojure.core :refer :all]
            [sisyphus.component.data-store :refer [get-data]]
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
                (let [{:keys [config etag valid? valid-message]} (get-data env config-key)]
                  (add-logger)
                  (info "endpoint config request")
                  (try                    
                    (if valid?
                      {:status 200
                       :headers {"Content-Type" "text/html; charset=utf-8"
                                 "etag" etag}
                       :body config}
             
                      {:status 500
                       :headers {"Content-Type" "text/html; charset=utf-8"}
                       :body valid-message})
                    (catch Exception e 
                      {:status 500
                       :headers {"Content-Type" "text/html; charset=utf-8"}
                       :body (.getMessage e)}))))))
