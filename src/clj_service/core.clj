(ns clj-service.core
  (:require
   [taoensso.timbre :as timbre :refer [info error]]
   [extension :refer [write-target-webly get-extensions]]
   [modular.permission.service :refer [add-permissioned-service]]
   [clj-service.websocket :refer [create-websocket-responder]]))

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
  [{:keys [services permission-service] :as this} {:keys [function permission fixed-args]
                                                   :or {fixed-args []
                                                        permission nil}}]
  (assert this "you need to pass the clj-service state")
  (assert permission-service "you need to pass the modular.permission.core state")
  (assert (symbol? function))
  (let [service-fn (resolve-symbol function)]
    (add-permissioned-service permission-service function permission)
    (swap! services assoc function {:service-fn service-fn
                                    :permission permission
                                    :fixed-args fixed-args})))

(defn expose-functions
  "exposes multiple functions with the same permission and fixed-args."
  [this
   {:keys [symbols permission fixed-args name]
    :or {permission nil
         fixed-args []
         name "services"}}]
  (assert (vector? symbols))
  (info "exposing [" name "]   permission: " permission " functions: " symbols)
  (doall
   (map (fn [function]
          (expose-function this {:function function
                                 :permission permission
                                 :fixed-args fixed-args})) symbols)))

; services list

(defn services-list [{:keys [services] :as _this}]
  (keys @services))

; start service

(defn- exts->services [exts]
  (->> (get-extensions exts {:clj-services nil})
       (map :clj-services)
       (remove nil?)))

(defn expose-stateless-services-from-extensions [this exts]
  (let [services (exts->services exts)]
    (write-target-webly :clj-services services)
    (doall
     (map #(expose-functions this %) services))))

(defn start-clj-services
  "starts the clj-service service.
   exposes stateless services that are discovered via the extension system.
   non stateless services need to be exposed via expose-service"
  [permission-service exts]
  (info "starting clj-services ..")
  (let [this {:services (atom {})
              :permission-service permission-service}]
    ; expose services list (which is stateful)
    (expose-function this
                     {:function 'clj-service.core/services-list
                      :permission nil
                      :fixed-args [this]})
    ; expose services from extensions (which are stateless)
    (info "exposing stateless services from extension..")
    (expose-stateless-services-from-extensions this exts)
    (info "creating websocket responder..")
    ; create websocket message handler
    (create-websocket-responder this)
    (info "clj-services running!")
    ; return the service state
    this))