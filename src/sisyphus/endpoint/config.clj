(ns sisyphus.endpoint.config
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [meta-merge.core :refer [meta-merge]]
            [sisyphus.schema :refer [build-schema validate-schema]]))


(def data-path "resources/data")

(def schema-data-path "resources/schema")

(defn merge-config 
 [s f]
 (reduce (fn [x y] (meta-merge x (f y))) {}  s))

(defn build-paths 
  [dirs]
  (for [i (range 1 (inc (count dirs)))]
    (str data-path "/" (clojure.string/join "/" (take i dirs)))))

(defn read-single-file 
  [filename]
  (-> filename
      io/file
      slurp
      edn/read-string))

(defn read-directory
  [dirname]
  (let [directory (io/file dirname)
        files (filter #(.isFile %) (file-seq directory))]
    (merge-config files read-single-file)))



(defn read-directories 
  [dirs]
  (let [paths (build-paths dirs)
        get-existing-dirs (take-while #(.isDirectory (io/file %)) paths)]
    get-existing-dirs
    (merge-config get-existing-dirs read-directory)))


(defn load-config
  [config-key]
  (let [dirs (clojure.string/split config-key #"/")]
    (read-directories dirs)))


(defn config-endpoint
  [config]
  (context "/config" []
           (GET ["/:env/:config-key" :config-key #".*"] 
                [env :<< keyword config-key :<< str]
                (let [config (get (load-config config-key) env nil)
                      schema (build-schema schema-data-path)]
                  (try                    
                    (if (and config (validate-schema schema config))
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
