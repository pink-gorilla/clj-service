(ns clj-service.handler
  (:require
   [taoensso.timbre :refer [info]]
   [ring.util.response :as res]
   [de.otto.nom.core :as nom]
   [clj-service.executor :refer [execute-with-binding *user*]]))

(defn service-handler
  [req]
  (info "service-api-handler: " req)
  (let [body-params (:body-params req)
        {:keys [fun args]} body-params
        _ (info "service: "  args)
        this nil
        user nil
        session nil
        r  (execute-with-binding this user session fun args)]
    (if (nom/anomaly? r)
      (res/bad-request r)
      (res/response r))))

