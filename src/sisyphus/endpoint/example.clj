(ns sisyphus.endpoint.example
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def config-filename "resources/data/example.edn")

(defn load-config []
  (-> config-filename
      io/file
      slurp
      edn/read-string))

(defn example-endpoint [config]
  (context "/example" []
    (GET "/:env" [env :<< keyword]
         (let [conf (get (load-config) env nil)]
           (if conf
             {:status 200
              :headers {"Content-Type" "text/html; charset=utf-8"}
              :body conf}
             {:status 404
              :headers {"Content-Type" "text/html; charset=utf-8"}
              :body "not found"})))))
