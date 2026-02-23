(ns clj-service.websocket
  (:require
   [clojure.string]
   [de.otto.nom.core :as nom]
   [taoensso.timbre :as timbre :refer [error]]
   [modular.permission.session :refer [get-user]]
   [modular.ws.msg-handler :refer [-event-msg-handler send-response]]
   [clj-service.executor :refer [execute-with-binding *user*]]))

(defn error-response [user fun args r]
  (error "clj-service websocket execution error: " r)
  {:error "Execution exception"
   :user user
   :fun fun
   :args args})

(defn response [r]
  {:result r})

(defn create-websocket-responder [{:keys [permission-service] :as this}]
  (defmethod -event-msg-handler :clj/service
    [{:keys [event _id _?data uid] :as req}]
    (let [[_ params] event ; _ is :clj/service
          {:keys [fun args]} params
          user (get-user permission-service uid)]
      (future
        (let [r (execute-with-binding this user uid fun args)]
          (if (nom/anomaly? r)
            (send-response req :clj/service (error-response user fun args r))
            (send-response req :clj/service (response r))))))))
