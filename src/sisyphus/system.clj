(ns sisyphus.system
  (:require [com.stuartsierra.component :as component]
            [duct.component.endpoint :refer [endpoint-component]]
            [duct.component.handler :refer [handler-component]]
            [duct.middleware.not-found :refer [wrap-not-found]]
            [meta-merge.core :refer [meta-merge]]
            [ring.component.jetty :refer [jetty-server]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [sisyphus.endpoint.config :refer [config-endpoint]]
            [sisyphus.component.scheduler :refer [scheduler-component]]
            [sisyphus.component.data-store :refer [data-store-component]]))

(def base-config
  {:app {:middleware [wrap-restful-format
                      [wrap-not-found :not-found]
                      [wrap-defaults :defaults]]
         :not-found  "Resource Not Found"
         :defaults   (meta-merge api-defaults {})}})

(defn new-system [config]
  (let [config (meta-merge base-config config)]
    (-> (component/system-map
         :app  (handler-component (:app config))
         :http (jetty-server (:http config))
         :config (endpoint-component config-endpoint)
         :scheduler (scheduler-component (:scheduler config))
         :data-store (data-store-component (:data-store config)))
        
        (component/system-using
         {:http [:app]
          :app  [:config]
          :config []
          :scheduler []
          :data-store []}))))
