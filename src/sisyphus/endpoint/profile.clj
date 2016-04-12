(ns sisyphus.endpoint.profile
  (:require [compojure.core :refer :all]
            [sisyphus.component.data-store :refer [get-profiles-structure]]))


(defn profile-endpoint
  [conf]
  (context "/profiles" []
           (GET ["/"]
                req
                {:headers {"Access-Control-Allow-Origin" "*"
                           "Access-Control-Allow-Methods" "GET,PUT,POST,DELETE,OPTIONS"
                           "Access-Control-Allow-Headers" "X-Requested-With,Content-Type,Cache-Control"}
                 :body (get-profiles-structure)})))
