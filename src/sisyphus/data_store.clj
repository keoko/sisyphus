(ns sisyphus.data-store
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [meta-merge.core :refer [meta-merge]]))

(def data-path "resources/data")

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

(defn load-data
  [config-key]
  (let [dirs (clojure.string/split config-key #"/")]
    (read-directories dirs)))

