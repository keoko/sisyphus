(ns sisyphus.endpoint.example-test
  (:require [com.stuartsierra.component :as component]
            [clojure.test :refer :all]
            [kerodon.core :refer :all]
            [kerodon.test :refer :all]
            [sisyphus.endpoint.config :as config]))

(def handler
  (config/config-endpoint {}))

(deftest smoke-test
  (testing "example page exists"
    (-> (session handler)
        (visit "/config")
        (has (status? 200) "page exists"))))
