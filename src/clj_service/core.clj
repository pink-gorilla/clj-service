(ns clj-service.core
  (:require
   [clojure.set :refer [rename-keys]]
   [taoensso.timbre :as timbre :refer [info error]]
   [extension :refer [write-target-webly get-extensions]]
   [modular.permission.service :refer [add-permissioned-service]]
   [modular.clj-service.websocket :refer [create-websocket-responder]]))

;; EXPOSE FUNCTION

(defn- resolve-symbol [s]
  (try
    (requiring-resolve s)
    (catch Exception ex
      (error "Exception in exposing service " s " - symbol cannot be required.")
      (throw ex))))

(defn expose-function
  "exposes one function 
   services args: this - created via clj-service.core
                  permission-service - created via modular.permission.core/start-permissions
   function args: service - fully qualified symbol
                  permission - a set following modular.permission role based access
                  fixed-args - fixed args to be passed to the function executor as the beginning arguments"
  [this permission-service {:keys [function permission fixed-args]
                            :or {fixed-args []
                                 permission nil}}]
  (assert this "you need to pass the clj-service state")
  (assert permission-service "you need to pass the modular.permission.core state")
  (assert (symbol? function))
  (let [service-fn (resolve-symbol function)]
    (add-permissioned-service permission-service function permission)
    (swap! this assoc function {:service-fn service-fn
                                :permission permission
                                :fixed-args fixed-args})))

(defn expose-functions
  "exposes multiple functions with the same permission and fixed-args."
  [this permission-service
   {:keys [function-symbols permission fixed-args name]
    :or {permission nil
         fixed-args []
         name "services"}}]
  (assert (vector? function-symbols))
  (info "exposing [" name "]   permission: " permission " functions: " function-symbols)
  (doall
   (map (fn [function]
          (expose-function this permission-service {:function function
                                                    :permission permission
                                                    :fixed-args fixed-args})) function-symbols)))

; services list

(defn services-list [this]
  (keys @this))

; start service

(defn- exts->services [exts]
  (->> (get-extensions exts {:clj-services nil})
       (map :clj-services)
       (remove nil?)))

(defn start-clj-services
  "starts the clj-service service.
   exposes stateless services that are discovered via the extension system.
   non stateless services need to be exposed via expose-service"
  [permission-service exts]
  (info "starting clj-services ..")
  (let [this (atom {})
        services (exts->services exts)]
    (write-target-webly :clj-services services)
    ; expose services list (which is stateful)
    (expose-function this permission-service
                     {:service-fn 'clj-service.core/services-list
                      :permission nil
                      :fixed-args [this]})
    ; expose stateless services discovered via extension-manager
    (doall
     (map (fn [clj-service]
            (expose-functions
             this permission-service
             (rename-keys clj-service {:symbols :function-symbols})))
          services))
    ; create websocket message handler
    (create-websocket-responder this permission-service)
    ; return the service state
    this))