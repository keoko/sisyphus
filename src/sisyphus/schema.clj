(ns sisyphus.schema
  (:require [schema.core :as s]
            [schema.utils :as su]
            [schema.coerce :as coerce]
            [clojure.java.io :as io]))

;; @todo read-string security issue???
(defn- load-schema
  [filename]
  (require '[schema.core :as s])
  (-> filename
      io/file
      slurp
      read-string
      eval))


(defn load-schemas
  [dirname]
  (let [dir (io/file dirname)
        files (filter #(.isFile %) (file-seq dir))]    
    (apply merge (map #(hash-map (keyword (.getName %)) (load-schema %)) files))))


(defn- merge-schemas 
  [dirname]
  (let [directory (io/file dirname)
        files (filter #(.isFile %) (file-seq directory))]
    (apply merge (map load-schema files))))


(defn merge-all-schemas
  [path]
  (merge-schemas path))

(defn validate-schema
  [schema config]
  (s/validate schema config))
