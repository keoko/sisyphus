(ns sisyphus.data-store
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [meta-merge.core :refer [meta-merge]]
   [clj-jgit.porcelain :as git]
   [sisyphus.config :as config]
   ))

(def data-path "resources/data")
(def repos-base-path "resources/data")

(defn merge-config 
 [s f]
  (reduce (fn [x y] (meta-merge x (f y))) {}  s))

(defn build-paths 
  [dirs base-dir]
  (let [base-path (.getCanonicalPath base-dir)
        paths (for [i (range 1 (inc (count dirs)))]
                (io/file (str base-path "/" (clojure.string/join "/" (take i dirs)))))]
    (conj paths base-dir)
))

(defn read-single-file 
  [file]
  (-> file
      slurp
      edn/read-string))

(defn read-directory
  [dir]
  (let [files (filter #(.isFile %) (.listFiles dir))]
    files
    (merge-config files read-single-file)))


(defn read-directories 
  [dirs base-dir]
  (let [paths (build-paths dirs base-dir)
        get-existing-dirs (take-while #(.isDirectory %) paths)]
    (merge-config get-existing-dirs read-directory)))


(defn get-env-dir!
  [env]
  (let [repo-dir-name (get-in config/defaults [:repositories env :dir])
        url (get-in config/defaults [:repositories env :url])
        dir-name (str repos-base-path "/" repo-dir-name)
        dir (io/file dir-name)]
    (if (not (.listFiles dir))
      (git/git-clone-full url dir-name))
    dir))

(defn update-env-dir!
  [env dir]
  (let [repo (git/load-repo dir)
        branch (get-in config/defaults [:repositories env :branch])]
    (git/git-pull repo)))

(defn load-data
  [config-key env]
  (let [dirs (clojure.string/split config-key #"/")
        base-dir (get-env-dir! env)        
        base-path (str data-path "/" (name env))]
    (update-env-dir! env base-dir)
    (read-directories dirs base-dir)))
