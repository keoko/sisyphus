(ns sisyphus.component.scheduler
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre
             :refer (info)]
            [chime :refer [chime-at]]
            [clj-time.core :as t]
            [clj-time.periodic :refer [periodic-seq]]
            [sisyphus.repository :as repo]
            [clojure.core.async :as async :refer [>!!]]))


(defrecord SchedulerComponent [connection chan]
  component/Lifecycle
  (start [component]
    (let [every-10-secs (rest (periodic-seq (t/now)
                                           (-> 10 t/seconds)))
          stop-scheduler (chime-at every-10-secs (fn [time] 
                                                   (doall
                                                    (map
                                                     (fn [x] (>!! chan x))
                                                     (repo/update-repos time)))))]
      (info "starting scheduler")
      (assoc component :stop-fn stop-scheduler)))
  (stop [component]
    (when-let [stop-fn (get component :stop-fn)]
      (info "stopping scheduler")
      (stop-fn))
    (dissoc component :stop-fn)))

(defn scheduler-component [connection chan]
  (->SchedulerComponent connection chan))



