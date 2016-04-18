(ns sisyphus.component.data-store
  (:require [sisyphus.schema :refer [validate-schema load-schemas]]
            [sisyphus.repository :refer [get-file push-file]]
            [com.stuartsierra.component :as component]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [meta-merge.core :refer [meta-merge]]
            [clj-yaml.core :as yaml]
            [taoensso.timbre :as timbre
             :refer (info)]
            [clojure.core.async :as async :refer [go-loop <!!]]))

(declare validate-profile)

(def data-path "/tmp/")

(def data-store (atom {}))


(def default-extensions
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


(defn build-paths 
  [dirs base-dir]
  (let [base-path (.getCanonicalPath base-dir)
        paths (for [i (range 1 (inc (count dirs)))]
                (io/file (str base-path "/" (clojure.string/join "/" (take i dirs)))))]
    (conj paths base-dir)))

(defn read-single-file 
  [file]
  (try 
    (let [parser (get-parser (.getName file) default-extensions)]
      (-> file
          slurp
          parser))
    (catch Exception e nil)))


(defn get-base-dir
  [profile]
  (let [dirname (str data-path "/" (name profile))]
    (io/file dirname)))

(defn get-data-dir
  [profile]
  (let [base-dir (get-base-dir profile)
        dirname (str (.getCanonicalPath base-dir) "/data")]
    (io/file dirname)))

(defn get-schema-dir
  [profile]
  (let [base-dir (get-base-dir profile)
        dirname (str (.getCanonicalPath base-dir) "/schema")]
    (io/file dirname)))



(defn get-extension [f]
  (-> (.getName f)
      (clojure.string/split #"\.")
      last))

(defn filter-supported-formats [fs]
  (filter #(get default-extensions (get-extension %)) fs))

(defn load-data
  [dir]
  (let [files (->> (.listFiles dir) 
                  (filter #(.isFile %))
                  filter-supported-formats
                  (sort-by #(.getName %)))]
    (apply meta-merge (map #(hash-map (keyword (.getName %)) (read-single-file %)) files))))


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
  (let [base-dir (get-data-dir profile)]
    (timbre/debug (str "loading data... " profile))
    (load-dir base-dir)))



(defn validate-config-group
  [schema config-group]
  (when schema
    (try
      (validate-schema schema config-group)
      nil ;; return nil, if there it's valid!!!
      (catch Exception e (.getMessage e)))))

(defn validate-root-config
  [schemas data]
  (let [validations  
        (map (fn [[k v]] (validate-config-group (get schemas k) v)) data)]
    {:valid? (every? nil? validations)
     :valid-message (clojure.string/join #"\n" (remove nil? validations))}))

(defn validate-variants-config
  [schemas variants root-data]
  (apply merge (map (fn [[k v]] 
                      {k (validate-profile schemas v root-data)}) 
                    variants)))


(defn validate-profile
  [schemas data root-data]
  (let [merged-data (meta-merge root-data (:data data))
        root-validation (validate-root-config schemas merged-data)
        variants-validation (when (seq  (:variants data)) 
                        (validate-variants-config schemas (:variants data) merged-data))]
    (into data
          [root-validation {:variants variants-validation}])))



(defn validate-all-data
  [data profile]
  (let [schemas (load-schemas (get-schema-dir profile))]
    (timbre/debug (str "validating data..."))
    (validate-profile schemas data {})))


(defn load-and-validate-all-data!
  [profile version]

  (let [data (load-all-data profile)]
    (swap! data-store 
           assoc 
           profile 
           (into {:version version}
                 (validate-all-data data profile)))))


(defn rebuild-data-store
  [profile version]
  (info (str "rebuilding data-store -> profile:" profile ", version:" version))
  (if (get @data-store profile)
    (when (not= version (get-in  @data-store [profile :version]))
      (info (str "swapping data store ..." version " ---- "(get-in @data-store [profile :version])))
      (load-and-validate-all-data! profile version))
    (do
      (info "repo not found, load into data-store")
      (load-and-validate-all-data! profile version))))


(defn watch-files
  [data-store-chan]
  (go-loop []
    (let [[repo-name repo-version] (<!! data-store-chan)]
      (rebuild-data-store repo-name repo-version)
      (recur))))


(defn build-variant-data-keys
  [profile variant key]
   (let  [variants (map #(keyword %) (clojure.string/split variant #"/"))]
    (for [i (range 1 (inc (count variants)))]
      (-> (interpose :variants (take i variants))
           (conj :variants)
           (conj profile)
           (concat [key])))))


(defn build-data-keys
  [profile variant key]
  (let [root-key [profile key]]
    (if (= "" variant)
      [root-key]
      (conj
       (build-variant-data-keys profile variant key)
       root-key))))


(defn get-data
  [profile variant]
  (info (str "profile:" profile ",variant:" variant))
  (let [data-keys (build-data-keys profile variant :data)
        valid-keys (build-data-keys profile variant :valid?)
        valid-message-keys (build-data-keys profile variant :valid-message)
        variants-data (map #(get-in @data-store %) data-keys)
        variants-valid (map #(get-in @data-store %) valid-keys)
        variants-valid-message (->> valid-message-keys
                                   (map #(get-in @data-store %))
                                   (remove empty?))]
    (info (str "variants:" keys))
    (timbre/info (str "valid message:" (vec variants-valid-message)))
    {:etag (get-in @data-store [profile :version])
     :valid?  (every? true? variants-valid)
     :valid-message (clojure.string/join #"," variants-valid-message)
     :config (apply meta-merge (vals (apply meta-merge variants-data)))}))


(defn get-group
  [profile variant group]
  (get-file profile variant (name group)))


(defn save-group [profile variant group data]
  (push-file profile variant (name group) data))

(defn get-group-structure [g variant-id]
  (let [group-ids (keys g)]
    {variant-id group-ids}))

(defn get-variants-structure [p parent-variant-id]
  (let [full-variant-id (fn [p v] (str (name p) (name v) "/"))]
    (meta-merge (get-group-structure (:data p) parent-variant-id)
                (map (fn [[k v]] (get-variants-structure v (full-variant-id parent-variant-id k))) (:variants p)))))

(defn get-profiles-structure []
  (->> (map (fn [[k v]] {k (get-variants-structure v "/")}) @data-store)
      flatten
      (apply merge)))

(defn profile-found?
  [profile]
  (nil? (:etag profile)))
