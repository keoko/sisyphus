(ns sisyphus.endpoint.config
  (:require [compojure.core :refer :all]
            [sisyphus.component.data-store :refer [get-data profile-found?]]
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
           (GET ["/:profile-id/:variant-path" :variant-path #".*"] 
                [profile-id :<< keyword 
                 variant-path :<< str]
                (let [{:keys [config etag valid? valid-message] :as profile} (get-data profile-id variant-path)]
                  (add-logger)
                  (info "endpoint config request")
                  (try
                    (cond
                      (profile-found? profile) {:status 404
                                                :headers {"Content-Type" "application/json"}}
                      valid? {:status 200
                              :headers {"Cache-Control" "public"
                                        "etag" etag}
                              :body config}
                      :else {:status 500
                             :headers {"Content-Type" "application/json"}
                             :body valid-message})
                    (catch Exception e 
                      {:status 500
                       :headers {"Content-Type" "application/json"}
                       :body (.getMessage e)}))))))
