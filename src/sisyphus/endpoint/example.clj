(ns sisyphus.endpoint.example
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [schema.core :as s]
            [schema.utils :as su]
            [schema.coerce :as coerce]
            [meta-merge.core :refer [meta-merge]]))


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


;; @todo read-string security issue???
(defn load-schema
  [filename]
  (-> filename
      io/file
      slurp
      read-string
      eval))

(defn merge-schemas 
  [dirname]
  (let [directory (io/file dirname)
        files (filter #(.isFile %) (file-seq directory))]
    ;;(load-schema "resources/schema/default.clj")
    (apply merge (map load-schema files))))

(defn read-directories 
  [dirs]
  (let [paths (build-paths dirs)
        get-existing-dirs (take-while #(.isDirectory (io/file %)) paths)]
    get-existing-dirs
    (merge-config get-existing-dirs read-directory)))

(defn load-files2 
  [path f]
  (let [files (->> path java.io.File. file-seq (sort-by f))]
  (doseq [x files]
    (when (.isFile x)
      (println (.getCanonicalPath x))))))


(defn load-config
  [config-key]
  (let [dirs (clojure.string/split config-key #"/")]
    (read-directories dirs)))


(defn example-endpoint
  [config]
  (context "/example" []
           (GET ["/:env/:config-key" :config-key #".*"] 
                [env :<< keyword config-key :<< str]
                (let [merged-config (get (load-config config-key) env nil)
                      merged-schema (merge-schemas schema-data-path)]
                  (try                    
                    (if (and merged-config (s/validate merged-schema merged-config))
                      {:status 200
                       :headers {"Content-Type" "text/html; charset=utf-8"}
                       :body (str merged-config)}
               
                      {:status 404
                       :headers {"Content-Type" "text/html; charset=utf-8"}
                       :body (str  "not found or invalid:" (su/error-val config))})
                    (catch Exception e 
                      {:status 500
                       :headers {"Content-Type" "text/html; charset=utf-8"}
                       :body (.getMessage e)}))))))
