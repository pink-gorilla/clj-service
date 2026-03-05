(ns clj-service.executor
  (:require
   [taoensso.timbre :refer [error]]
   [clj-service.core :refer [call-fn get-service]]
   [clj-service.permission :refer [authorized?]]))

;; USER

(defonce ^:dynamic
  ^{:doc "The session-id for which a clj-service gets executed"}
  *session* nil)

(defonce ^:dynamic
  ^{:doc "The user-id for which a clj-service gets executed"}
  *user* nil)

(defn run-fn [service clj-call]
  (try
    (call-fn service clj-call)
    (catch clojure.lang.ExceptionInfo e
      (let [msg (or (ex-message e)
                    (str "exception running " (:fun clj-call)))]
        (throw (ex-info msg
                        {:error (pr-str e)
                         :clj-call clj-call}))))
    (catch AssertionError  e
      (throw (ex-info (str "run " (:fun clj-call) " assert error")
                      {:error (pr-str e)})))))

(defn run-with-binding [{:keys [user session] :as setup}
                        service
                        clj-call]
  (binding [*user* user
            *session* session]
    (run-fn service clj-call)))

(defn execute
  "executes a service-fn with args + fixed-args
   returns the result or throws"
  [{:keys [clj token] :as ctx}
   {:keys [user session] :as setup}
   {:keys [fun] :as clj-call}]
  (let [service (get-service clj clj-call)]
    (if (authorized? service user)
      (run-with-binding setup service clj-call)
      (throw (ex-info (str "user " (:user user) " not authorized for: " fun)
                      {:fun fun
                       :user user})))))

