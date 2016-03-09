(ns sisyphus.endpoint.example
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [schema.core :as s]
            [meta-merge.core :refer [meta-merge]]))


(s/defschema Config [s/Str])

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

(defn validate-config [config]
  (print config)
  (s/validate Config config))

(defn example-endpoint [config]
  (context "/example" []
    (GET "/:env" [env :<< keyword]
         (let [config (get (load-config) env nil)]
           (if config
             (if (validate-config config)
               {:status 200
                :headers {"Content-Type" "text/html; charset=utf-8"}
                :body (str config)}
               {:status 500
                :headers {"Content-Type" "text/html; charset=utf-8"}
                :body (validate-config config)
                })
             {:status 404
              :headers {"Content-Type" "text/html; charset=utf-8"}
              :body "not found"})))))
