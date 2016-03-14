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
  [dirs base-path]
  (for [i (range 1 (inc (count dirs)))]
    (str base-path "/" (clojure.string/join "/" (take i dirs)))))

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
  [dirs env]
  (let [base-path (str data-path "/" (name env))
        paths (build-paths dirs base-path)
        get-existing-dirs (take-while #(.isDirectory (io/file %)) paths)]
    (merge-config get-existing-dirs read-directory)))

(defn load-data
  [config-key env]
  (let [dirs (clojure.string/split config-key #"/")]
    (read-directories dirs env)))

