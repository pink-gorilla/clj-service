(ns clj-service.http-handler
  (:require
   [taoensso.timbre :refer [debug]]
   [ring.util.response :as res]
   [missionary.core :as m]
   [clj-service.executor :refer [execute-wrapped]]
))

(defn service-handler
  [{:keys [identity browser-id ctx body-params] :as req}]
  (let [;_ (info "browser-id: " browser-id)
          ;_ (info "identity: " identity)
          ;_ (info "ctx keys:" (keys ctx))
          ;_ (info "body-params: " body-params)
        {:keys [fun args] :as clj-call} body-params
        _ (debug "clj-http-service: running fun:" fun " with args: " args)
        r (m/? (execute-wrapped ctx {:user identity :session browser-id} clj-call))]
    (res/response r)))
