(ns sisyphus.endpoint.group
  (:require [compojure.core :refer :all]
            [sisyphus.component.data-store :refer [get-group save-group]]
            [clojure.string :refer [join split]]))


(defn split-variant-and-group
  [s]
  (let [parts (split s #"/")
        variant-id (->> parts
                       butlast
                       (join "/"))
        group-id (last parts)]
    [variant-id group-id]))

;; URL:
;;   / -> invalid
;;  /{profile}/ -> WRONG
;;  /{profile}/{group} 7-> OK
;;  /{profile}/{variant}/{group} -> OK
;;  /{profile}/{variant}/{sub-variant}/{group} -> OK
(defn group-endpoint
  [conf]
  (context "/group" []
           (GET ["/:profile-id/:variant-and-group" :variant-and-group #".*"]
                [profile-id :<< keyword
                 variant-and-group :<< str] 
                (try 
                  (let [[variant-id group-id] (split-variant-and-group variant-and-group)
                        group (get-group profile-id variant-id group-id)]
                    {:headers {"Access-Control-Allow-Origin" "*"
                               "Access-Control-Allow-Methods" "GET,PUT,POST,DELETE,OPTIONS"
                               "Access-Control-Allow-Headers" "X-Requested-With,Content-Type,Cache-Control"}
                     :body {:profile-id profile-id
                            :variant-id variant-id
                            :group-id group-id
                            :data group}})
                  (catch java.io.FileNotFoundException e
                    {:headers {"Access-Control-Allow-Origin" "*"
                               "Access-Control-Allow-Methods" "GET,PUT,POST,DELETE,OPTIONS"
                               "Access-Control-Allow-Headers" "X-Requested-With,Content-Type,Cache-Control"}
                     :status 404
                     :body (.getMessage e)})
                  (catch Exception e
                    {:headers {"Access-Control-Allow-Origin" "*"
                               "Access-Control-Allow-Methods" "GET,PUT,POST,DELETE,OPTIONS"
                               "Access-Control-Allow-Headers" "X-Requested-With,Content-Type,Cache-Control"}
                     :status 500
                     :body (.getMessage e)})))

           (POST ["/:profile-id/:variant-and-group" :variant-and-group #".*"]
                [profile-id :<< keyword 
                 variant-and-group :<< str
                 group-data :<< str]
                (let [[variant-id group-id] (split-variant-and-group variant-and-group)
                      saved? (save-group profile-id variant-id group-id group-data)]
                    {:headers {"Access-Control-Allow-Origin" "*"
                               "Access-Control-Allow-Methods" "GET,PUT,POST,DELETE,OPTIONS"
                               "Access-Control-Allow-Headers" "X-Requested-With,Content-Type,Cache-Control"}
                     :body [profile-id variant-id group-id group-data]
                     :status (if saved? 200 500)}))
           ;; ajax.core request OPTIONS before a POST, not sure why.
           (OPTIONS ["/:profile-id/:variant-and-group" :variant-and-group #".*"]
                    req
                 {:headers {"Access-Control-Allow-Origin" "*"
                            "Access-Control-Allow-Methods" "GET,PUT,POST,DELETE,OPTIONS"
                            "Access-Control-Allow-Headers" "X-Requested-With,Content-Type,Cache-Control"}
                  :status 200
                  :body {}})))
