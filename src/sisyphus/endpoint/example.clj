(ns sisyphus.endpoint.example
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [schema.core :as s]
            [meta-merge.core :refer [meta-merge]]))


(def data-path "resources/data")


(s/defschema Config {s/Keyword s/Any
                     (s/optional-key :zar) s/Keyword})


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

(defn validate-config
  [config]
  (s/validate Config config))

(defn example-endpoint
  [config]
  (context "/example" []
           (GET ["/:env/:config-key" :config-key #".*"] 
                [env :<< keyword config-key :<< str]
                (let [config (get (load-config config-key) env nil)]
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
                       :body (.getMessage e)}))))))
