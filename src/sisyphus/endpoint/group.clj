(ns sisyphus.endpoint.group
  (:require [compojure.core :refer :all]
            [sisyphus.component.data-store :refer [get-group save-group]]))


;; URL:
;;   / -> invalid
;;  /{profile}/ -> WRONG
;;  /{profile}/{group} 7-> OK
;;  /{profile}/{variant}/{group} -> OK
;;  /{profile}/{variant}/{sub-variant}/{group} -> OK
(defn group-endpoint
  [conf]
  (context "/group" []
           (GET ["/:profile-id/:group-id" :group-id #".*"]
                [profile-id :<< keyword 
                 group-id :<< keyword] 
                (let [variant-id "" ;; todo
                      group (get-group profile-id variant-id group-id)]
                    {:headers {"Access-Control-Allow-Origin" "*"
                               "Access-Control-Allow-Methods" "GET,PUT,POST,DELETE,OPTIONS"
                               "Access-Control-Allow-Headers" "X-Requested-With,Content-Type,Cache-Control"}
                     :body {:profile-id profile-id
                            :group-id group-id
                            :data "#YAML file
- job1
- job2
- job3
- job4"}}))
           (POST ["/:profile-id/:variant-id/:group-id" :group-id #".*"]
                [profile-id :<< keyword 
                 variant-id :<< str
                 group-id :<< keyword
                 group-data :<< str]
                (let [saved? (save-group profile-id variant-id group-id group-data)]
                    {:headers {"Access-Control-Allow-Origin" "*"
                               "Access-Control-Allow-Methods" "GET,PUT,POST,DELETE,OPTIONS"
                               "Access-Control-Allow-Headers" "X-Requested-With,Content-Type,Cache-Control"}
                     :body [profile-id variant-id group-id group-data]
                     :status (if saved? 200 500)}))
           ;; ajax.core request OPTIONS before a POST, not sure why.
           (OPTIONS ["/:profile-id/:variant-id/:group-id" :group-id #".*"]
                    req
                 {:headers {"Access-Control-Allow-Origin" "*"
                            "Access-Control-Allow-Methods" "GET,PUT,POST,DELETE,OPTIONS"
                            "Access-Control-Allow-Headers" "X-Requested-With,Content-Type,Cache-Control"}
                  :status 200
                  :body {}})))
