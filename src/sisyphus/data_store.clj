(ns sisyphus.data-store
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [meta-merge.core :refer [meta-merge]]
   [clj-yaml.core :as yaml]
   [taoensso.timbre :as timbre
    :refer (debug)]))

(def data-path "/tmp/")

(def default-extensions
  "The default mapping from file extension to a [[ConfigParser]] for content from such a file.

  Provides parsers for the \"yaml\" and \"edn\" extensions."
  {"yaml" #(yaml/parse-string % true)
   "yml" #(yaml/parse-string % true)
   "edn"  edn/read-string
   "clj" edn/read-string})

(defn- get-parser 
  [^String path extensions]
  (let [dotx      (.lastIndexOf path ".")
        extension (subs path (inc dotx))]
    (or (get extensions extension)
        (throw (ex-info "Unknown extension for configuration file."
                        {:path       path
                         :extensions extensions})))))

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
  (let [parser (get-parser (.getName file) default-extensions)]
    (-> file
        slurp
        parser)))

(defn read-directory
  [dir]
  (let [files (->> (.listFiles dir) 
                  (filter #(.isFile %))
                  (sort-by #(.getName %)))]
    files
    (merge-config files read-single-file)))


(defn read-directories 
  [dirs base-dir]
  (let [paths (build-paths dirs base-dir)
        get-existing-dirs (take-while #(.isDirectory %) paths)]
    (merge-config get-existing-dirs read-directory)))


(defn get-base-dir
  [env]
  (let [dir-name (str data-path "/app1-" (name env))]
    (io/file dir-name)))

(defn load-data
  [config-key env]
  (let [dirs (clojure.string/split config-key #"/")
        base-dir (get-base-dir env)]
    (timbre/debug (str  "loading data ... " env))
    (read-directories dirs base-dir)))
