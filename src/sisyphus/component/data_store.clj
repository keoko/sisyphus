(ns sisyphus.component.data-store
  (:require [com.stuartsierra.component :as component]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [meta-merge.core :refer [meta-merge]]
            [clj-yaml.core :as yaml]
            [taoensso.timbre :as timbre
             :refer (info)]
            [clojure.core.async :as async :refer [go-loop <!!]]))

(def data-path "/tmp/")

(def data-store (atom {}))


(def default-extensions
  "The default mapping from file extension to a [[ConfigParser]] for content from such a file.

  Provides parsers for the \"yaml\" and \"edn\" extensions."
  {"yaml" #(yaml/parse-string % true)
   "yml" #(yaml/parse-string % true)
   "edn"  edn/read-string
   "clj" edn/read-string})


(declare watch-files
         load-variants)

(defrecord DataStoreComponent [connection chan]
  component/Lifecycle
  (start [component]
    (let []
      (info "starting data-store")
      (watch-files chan)
      component))
  (stop [component]
    (info "stopping data-store")
    component))

(defn data-store-component [connection chan]
  (->DataStoreComponent connection chan))

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
  (let [dir-name (str data-path "/" (name env))]
    (io/file dir-name)))


(defn load-data-old
  [config-key env]
  (let [dirs (clojure.string/split config-key #"/")
        base-dir (get-base-dir env)]
    (timbre/debug (str  "loading data ... " env))
    (read-directories dirs base-dir)))


;; new ones

(defn load-data
  [dir]
  (let [files (->> (.listFiles dir) 
                  (filter #(.isFile %))
                  (sort-by #(.getName %)))]
    (apply merge (map #(hash-map (keyword (.getName %)) (read-single-file %)) files))))


(defn load-dir
 [dir]
 {:data (load-data dir)
  :variants (load-variants dir)})



(defn load-variants
  [dir]
  (let [dirs (->> (.listFiles dir) 
                  (filter #(.isDirectory %))
                  (remove #(re-matches #"^\..*" (.getName %))))]
    (apply merge (map #(hash-map (keyword (.getName %)) (load-dir %)) dirs))))


(defn load-all-data
  [profile]
  (let [base-dir (get-base-dir profile)]
    (timbre/debug (str "loading data... " profile))
    (load-dir base-dir)))


(defn rebuild-data-store
  [profile version]
  (info (str "profile:" profile ", version:" version))
  (if (get  @data-store profile)
    (when (not= version  (get-in  @data-store [profile :version]))
      (do
        (info (str "swapping data store ..." version " ---- "(get-in @data-store [profile :version])))
        (swap! data-store assoc profile (into {:version version}
                                              (load-all-data profile)))))
    (do
      (info "repo not found")
      (swap! data-store assoc profile (into  {:version version}
                                             (load-all-data profile))))))


(defn watch-files
  [data-store-chan]
  (go-loop []
    (let [[repo-name repo-version] (<!! data-store-chan)]
      (info (str "msg:" repo-name repo-version))
      (rebuild-data-store repo-name repo-version)
      (recur))))


(defn build-variant-data-keys
  [profile variant]
   (let  [variants (map #(keyword %) (clojure.string/split variant #"/"))]
    (for [i (range 1 (inc (count variants)))]
      (-> (interpose :variants (take i variants))
           (conj :variants)
           (conj profile)
           (concat [:data])))))


(defn build-data-keys
  [profile variant]
  (conj
   (build-variant-data-keys profile variant)
   [profile :data]))


(defn get-data
  [profile variant]
  (info (str "1profile:" profile ",variant:" variant))
  (let [keys (build-data-keys profile variant)
        variants-data (map #(get-in @data-store %) keys)]
    (apply meta-merge (vals (apply meta-merge variants-data)))))
