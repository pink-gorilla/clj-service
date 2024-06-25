(ns service-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [de.otto.nom.core :as nom]
   [extension :refer [discover]]
   [modular.permission.core :refer [start-permissions]]
   [clj-service.core :as s :refer [start-clj-services expose-functions services-list]]
   [clj-service.executor :refer [execute]]))

(def p  (start-permissions {})) ; empty map means no permissions

(def this (start-clj-services p (discover)))

(expose-functions this {:name "demo-static"
                        :symbols ['funs/fun-noargs
                                  'funs/fun-add
                                  'funs/fun-ex]
                        :permission nil
                        :fixed-args []})

(expose-functions this {:name "demo-service"
                        :symbols ['funs/fun-service]
                        :permission nil
                        :fixed-args [[:a :b :c]]})

(println "exposed functions: " (services-list this))

(defn run [fun & args]
  (let [user nil]
    (execute this user fun args)))

(deftest services-test
  (testing "services test"
    (let [a (run 'funs/fun-noargs)
          b (run 'funs/fun-add 2 7)
          c (run 'funs/fun-ex) ; called fn throws ex
          d (run 'funs/fun-add)  ; missing params
          e (run 'funs/unknown-function) ; unknown service
          f (run 'funs/fun-service [1 2 3]) ; unknown service
          ]
      (is (= a 27))
      (is (= b 9))
      (is (nom/anomaly? c))
      (is (nom/anomaly? d))
      (is (nom/anomaly? e))
      (is (= f [:a :b :c 1 2 3])))))