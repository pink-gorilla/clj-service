(ns clj-service.websocket
  (:require
   [clojure.string]
   [de.otto.nom.core :as nom]
   [modular.ws.core :refer [send-response]]
   [modular.ws.msg-handler :refer [-event-msg-handler]]
   [modular.permission.session :refer [get-user]]
   [clj-service.executor :refer [execute *user*]]))

(defn create-websocket-responder [this permission-service]
  (defmethod -event-msg-handler :clj/service
    [{:keys [event _id _?data uid] :as req}]
    (let [[_ params] event ; _ is :clj/service
          {:keys [fun args]} params
          user (get-user permission-service uid)]
      (future
        (let [r (binding [*user* user]
                  (execute this permission-service user fun args))]
          (if (nom/anomaly? r)
            (send-response req :clj/service  {:error "Execution exception"
                                              :uid uid})
            (send-response req :clj/service {:result r})))))))
