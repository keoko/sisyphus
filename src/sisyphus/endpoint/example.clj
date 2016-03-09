(ns sisyphus.endpoint.example
  (:require [compojure.core :refer :all]))

(def data
  {:dev "dev data"
   :stg "stg data"
   :prd "prd data"})

(defn example-endpoint [config]
  (context "/example" []
    (GET "/:env" [env :<< keyword]
         (let [conf (get data env nil)]
           (if conf
             {:status 200
              :headers {"Content-Type" "text/html; charset=utf-8"}
              :body conf}
             {:status 404
              :headers {"Content-Type" "text/html; charset=utf-8"}
              :body "not found"})))))
