(ns sisyphus.endpoint.example
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [meta-merge.core :refer [meta-merge]]))


(def default-config-filename "resources/data/default.edn")
(def config-filename "resources/data/example.edn")


(defn read-single-file [filename]
  (-> filename
      io/file
      slurp
      edn/read-string))

(defn load-config []
  (meta-merge
   (read-single-file default-config-filename)
   (read-single-file config-filename))
)

(defn example-endpoint [config]
  (context "/example" []
    (GET "/:env" [env :<< keyword]
         (let [conf (get (load-config) env nil)]
           (if conf
             {:status 200
              :headers {"Content-Type" "text/html; charset=utf-8"}
              :body (str conf)}
             {:status 404
              :headers {"Content-Type" "text/html; charset=utf-8"}
              :body "not found"})))))
