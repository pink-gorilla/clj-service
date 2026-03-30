(ns clj-service.executor
  (:require
   [taoensso.timbre :refer [error]]
   [missionary.core :as m]
   [clj-service.core :refer [call-fn get-service]]
   [clj-service.permission :refer [authorized?]]
   [clj-service.response :refer [response-success response-error]]
   ))

;; USER

(defonce ^:dynamic
  ^{:doc "The session-id for which a clj-service gets executed"}
  *session* nil)

(defonce ^:dynamic
  ^{:doc "The user-id for which a clj-service gets executed"}
  *user* nil)

(defn run-with-binding [{:keys [user session] :as setup}
                        {:keys [mode] :as service}
                        clj-call]
  (case mode
    :clj (m/via m/cpu
                (binding [*user* user
                          *session* session]
                  (call-fn service clj-call)))
    :sp (call-fn service clj-call)
    (throw (ex-info (str "unknown mode: " mode) clj-call))))


(defn execute
  "executes a service-fn with args + fixed-args
   returns the result or throws"
  [{:keys [clj token] :as ctx}
   {:keys [user session] :as setup}
   {:keys [fun] :as clj-call}]
  (m/sp
   (let [service (get-service clj clj-call)]
     (if (authorized? service user)
       (m/? (run-with-binding setup service clj-call))
       (throw (ex-info (str "user " (:user user) " not authorized for: " fun)
                       {:fun fun
                        :user user}))))))

(defn execute-wrapped [ctx setup clj-call]
  (m/sp
   (try
     (let [r (m/? (execute ctx setup clj-call))]
       (response-success r))
     (catch clojure.lang.ExceptionInfo ex
       (response-error ex))
     (catch Exception ex
       (response-error ex))
     (catch AssertionError e
       (response-error e)))))



