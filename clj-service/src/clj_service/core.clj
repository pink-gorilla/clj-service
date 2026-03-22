(ns clj-service.core
  (:require
   [taoensso.timbre :refer [debug debugf info infof warn error]]
   [extension :refer [get-extensions]]
   [modular.writer :refer [write-edn-private]]))

(defn- resolve-symbol [s]
  (try
    (requiring-resolve s)
    (catch Exception ex
      (error "Exception in exposing service " s " - symbol cannot be required.")
      (throw (ex-info (str "clj-service could not resolve service " s)
                      {:service s})))))

(defn get-farg [{:keys [env]} ctx]
  (cond
    (nil? ctx) []
    (keyword? ctx) [(get env ctx)]
    (set? ctx) [(select-keys env ctx)]
    (boolean? ctx) [env]
    :else ctx))

(defn expose
  "exposes one function 
   this - created via start-executor
   service-opts: 
      fun - fully qualified symbol
      fixed - a fixed args to be passed to a stateful function which is its first parameter"
  [{:keys [services] :as this} {:keys [fun ctx permission] :as service-opts}]
  (debug "exposing fun: " fun " ctx: " ctx " permission: " permission)
  (let [sfn (resolve-symbol fun)
        farg (get-farg this ctx)]
    (swap! services assoc fun (merge service-opts {:sfn sfn
                                                   :farg farg}))))

(defn get-ext-services [exts]
  (->> (get-extensions exts {:clj-service []})
       (map :clj-service)
       (apply concat)
       (into [])))

(defn env-clean [env]
  (->> env
       (map (fn [[k v]]
              [k (if (var? v) (var-get v) v)]))
       (into {})))

(defn start-clj-services
  "starts the clj-services
   :services - a vec of service-definition maps
   :env - the environment that gets (entirely or modified passed to a service that is stateful)"
  [{:keys [env exts app-services]
    :or {exts []
         env {}
         app-services []}}]
  (info "clj-services starting ..")
  (let [env (env-clean (or env {}))
        this {:env env
              :services (atom {})}
        services (concat (get-ext-services exts) app-services)]
    (write-edn-private "clj-services" services)
    (doall
     (for [service services]
       (expose this service)))
    (info "clj-services started!")
    ; return the service state
    this))

(defn get-service [{:keys [services] :as this} {:keys [fun] :as clj-call}]
  ;(info "get-service " fun " available:" (keys @services))
  (if-let [s (get @services fun)]
    s
    (throw (ex-info (str "service not found: " fun)
                    {:fun fun}))))

(defn call-fn [{:keys [sfn farg] :as this}
               {:keys [args]
                :or {args []}
                :as clj-call}]
  ;(println "clj-call: " clj-call " service: " service)
  ;(println "clj-call: " clj-call)
  (let [args (if farg
               (concat farg args)
               args)]
    (apply sfn args)))

