(ns demo.sleepy
  (:require 
     [missionary.core :as m]))


(defn happy? []
  (m/sp 
   (m/? (m/sleep 2000))
   "I am happy!"))

(defn ex-test []
  (throw (ex-info "I am bad" {:reason :just-bad}))
  )

(defn assert-test []
  (assert (= 42 0) "impossible!"))