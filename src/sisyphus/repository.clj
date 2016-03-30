(ns sisyphus.repository
  (:require 
   [sisyphus.config :as config]
   [clj-jgit.porcelain :as git]
   [sisyphus.config :as config]
   [clojure.java.io :as io]
   [taoensso.timbre :as timbre
    :refer (info)]
   [chime :refer [chime-at]]))


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
  [{:keys [url branch dir]}]
  ;; get repo dir
  (let [repo-dir (io/file dir)]
    (pr (str "dir:" dir (.isDirectory repo-dir)))
    (if (.isDirectory repo-dir)
      (pull-repo! repo-dir)
      (clone-repo! repo-dir url branch))
    [branch (get-repo-hash (git/load-repo repo-dir))]))

 
(defn update-repos
  [time]
  (info "updating repo")
  (doall (map update-repo repos)))
