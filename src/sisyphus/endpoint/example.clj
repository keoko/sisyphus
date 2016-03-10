(ns sisyphus.endpoint.example
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [schema.core :as s]
            [meta-merge.core :refer [meta-merge]]))


(s/defschema Config {s/Keyword s/Any
                     (s/optional-key :zar) s/Keyword})

(def default-config-filename "resources/data/default.edn")
(def config-filename "resources/data/example.edn")


(defn read-single-file [filename]
  (-> filename
      io/file
      slurp
      edn/read-string))

(defn read-directory [dirname]
  (let [directory (io/file dirname)
        files (file-seq directory)]
    (reduce (fn [x y] (meta-merge x (read-single-file y))) files)))

(defn load-config []
  (read-directory "resources/data")
  #_(meta-merge
   (read-single-file default-config-filename)
   (read-single-file config-filename))
)

(defn validate-config [config]
  (s/validate Config config))

(defn example-endpoint [config]
  (context "/example" []
           (GET ["/:env/:key" :key #".*"] [env :<< keyword & config-key]
                (let [config (get (load-config) env nil)]
                  (try
                    (if (and config (validate-config config))
                      {:status 200
                :headers {"Content-Type" "text/html; charset=utf-8"}
                :body (str config)}
               
               {:status 404
                :headers {"Content-Type" "text/html; charset=utf-8"}
                :body "not found"})
             (catch Exception e 
               {:status 500
                :headers {"Content-Type" "text/html; charset=utf-8"}
                :body (.getMessage e)
                }))))))
