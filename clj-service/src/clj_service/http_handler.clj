(ns clj-service.http-handler
  (:require
   [taoensso.timbre :refer [info]]
   [ring.util.response :as res]
   [clj-service.executor :refer [execute]]
   [clj-service.response :refer [response-success response-error]]))

(defn service-handler
  [{:keys [identity browser-id ctx body-params] :as req}]
  (try
    ;(info "service-handler: " (keys req))
    (let [;_ (info "browser-id: " browser-id)
          ;_ (info "identity: " identity)
          ;_ (info "ctx keys:" (keys ctx))
          ;_ (info "body-params: " body-params)
          {:keys [fun args] :as clj-call} body-params
          _ (info "clj-http-service: running fun:" fun " with args: " args)
          r (execute ctx {:user identity :session browser-id} clj-call)]
      (res/response (response-success r)))
    (catch Exception ex
      (res/response (response-error ex)))))
