(ns clj-service.handler
  (:require
   [taoensso.timbre :refer [info]]
   [ring.util.response :as res]
   [de.otto.nom.core :as nom]
   [modular.webserver.middleware.api :refer [wrap-api-handler]]
   [goldly.service.executor :refer [run-service *user*]]))

(defn service-handler
  [req]
  (info "service-api-handler: " req)
  (let [body-params (:body-params req)
        args (select-keys body-params [:fun :args])
        _ (info "service: "  args)
        response  (binding [*user* nil]
                    (run-service args))]
    (if (nom/anomaly? response)
      (res/bad-request response)
      (res/response response))))

(def service-handler-wrapped (wrap-api-handler service-handler))
