(ns clj-service.runner
  (:require
   [missionary.core :as m]
   [tick.core :as t]
   [human-id.core :refer [human-id]]
   [clj-service.core :as exec]
   [clj-service.logger :as l])
  (:import
   [missionary Cancelled]
   [java.time.format DateTimeFormatter]
   [java.time ZoneId]))

(defn start-sp [logger write service {:keys [id] :as clj-call}]
  (m/sp
   (try
     (l/log logger "start-sp:" clj-call)
     (let [v (exec/call-fn service clj-call)]
       ; success case
       (m/? (write {:op :exec :id id :val (m/? v)})))
     (catch Exception ex
       (m/? (write {:op :exec :id id :err (ex-message ex)}))))))

(defn start-clj [logger write service {:keys [id] :as clj-call}]
  (m/sp
   (try
     (l/log logger "start-clj:" clj-call)
     (let [v (m/via m/cpu (exec/call-fn service clj-call))]
      ; success case
       (m/? (write {:op :exec
                    :id id
                    :val (m/? v)})))
     (catch Exception ex
       (m/? (write {:op :exec :id id :err (ex-message ex)}))))))

(defn start-ap [logger write service {:keys [id] :as clj-call}]
  (l/log logger "start-ap:" clj-call)
  (when-let [f (exec/call-fn service clj-call)]
    (m/reduce (fn [_s v]
                (try
                  (m/? (write {:op :exec
                               :id id
                               :val v}))
                  (catch Cancelled _
                    (l/log logger "ap cancelled on ws close"))))
              nil f)))

(defn start-executing
  "returns a missionary task which can execute the clj-call"
  [logger write {:keys [mode] :as service} {:keys [id] :as clj-call}]
  (case mode
    :sp (start-sp logger write service clj-call)
    :ap (start-ap logger write service clj-call)
    :clj (start-clj logger write service clj-call)
    ; default :clj
    (start-clj logger write service clj-call)))