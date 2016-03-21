(ns sisyphus.component.scheduler
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre
             :refer (info)]
            [chime :refer [chime-at]]
            [clj-time.core :as t]
            [clj-time.periodic :refer [periodic-seq]]
            [clojure.core.async :as a :refer [<! go-loop]]))

(defrecord SchedulerComponent [connection]
  component/Lifecycle
  (start [component]
    (let [every-2-secs (rest (periodic-seq (t/now)
                                           (-> 2 t/seconds)))
          stop-scheduler (chime-at every-2-secs #(info (str "chiming at " %)))]
      (info "starting scheduler")
      (assoc component :stop-fn stop-scheduler)
      ))
  (stop [component]
    (let [stop-fn (:stop-fn component)]
      (info "stopping scheduler")
      (stop-fn))))

(defn scheduler-component [connection]
  (->SchedulerComponent connection))



