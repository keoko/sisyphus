(ns sisyphus.repository
  (:require 
   [sisyphus.config :as config]
   [clj-jgit.porcelain :as git]
   [sisyphus.config :as config]
   [clojure.java.io :as io]
   [taoensso.timbre :as timbre
    :refer (info error)]
   [chime :refer [chime-at]]))


(defn delete-dir
  [dir]
  (when (.isDirectory dir)
    (map #(delete-dir %) (.listFiles dir)))
  (.delete dir))

(def repos (get config/defaults :repositories))


(defn get-repo-hash
  [repo]
  (-> repo
      (.getRepository)
      (.resolve "HEAD^{tree}")
      (.getName)))


(defn clone-repo!
  [dir url branch]
  (let [canonical-dir (.getCanonicalPath dir)]
        (pr (str (.getName dir) "-" url "-" branch))
        (git/git-clone-full url canonical-dir)
        (git/with-repo canonical-dir
          (git/git-fetch-all repo)
          (git/git-checkout repo branch))))


(defn pull-repo!
  [dir]
  (let [repo (git/load-repo dir)]
    (git/git-pull repo)))


(defn update-repo
  [{:keys [env url branch dir]}]
  (info (str "update-repo env:" env))
  (let [repo-dir (io/file dir)]
    (info (str "dir:" dir  ", isdir?:" (.isDirectory repo-dir) ", env:" env))
    (if (.isDirectory repo-dir)
      (try 
        (pull-repo! repo-dir)
        (catch Exception e (error "cannot pull repo:" (.getMessage e))))
      (try
        (clone-repo! repo-dir url branch)
        (catch Exception e (do
                             (error "cannot clone repo: " (.getMessage e))
                             (delete-dir repo-dir)))))
    (info (str "vec:" env))
    [env (get-repo-hash (git/load-repo repo-dir))]))

 
(defn update-repos
  [time]
  (info "updating repo")
  (doall (pmap (fn [[k v]] (update-repo v)) repos)))


(defn get-file
  [repo-id dir filename]
  (let [repo-dir (:dir (get repos repo-id))
        full-filename (str repo-dir "/data/" dir "/" filename)]
    (slurp full-filename)))

(defn push-file
  [repo-id dir filename content]
  (let [repo-dir (:dir (get repos repo-id))
        relative-filename (if (empty? dir)
                            filename 
                            (str dir "/" filename))
        full-filename (str repo-dir "/data/" dir "/" filename)
        repo (git/load-repo repo-dir)]
    (io/make-parents full-filename)
    (spit full-filename content)
    (git/git-add repo relative-filename)
    (git/git-commit repo (str "changed " relative-filename))
    (-> repo
        (.push)
        (.call))))
