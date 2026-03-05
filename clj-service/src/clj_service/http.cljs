(ns clj-service.http
  (:require
   [taoensso.timbre :refer-macros [debug info error]]
   [promesa.core :as p]
   [ajax.core :refer [POST]] ; https://github.com/JulianBirch/cljs-ajax
   [transit.io :refer [read-opts]]))

(defn clj
  ([fun]
   (clj {} fun))
  ([fun & args]
   (let [[opts fun args] (if (map? fun)
                           [fun (first args) (rest args)]
                           [{} fun args])]
     (p/create
      (fn [resolve reject]
        (info "clj-http request for fun: " fun)
        (POST "/clj-service"
          {:params {:fun fun
                    :args args
                    :opts opts}
           :timeout 10000         ;; optional see API docs
           :response-format (ajax.core/transit-response-format :json (read-opts))
           :handler (fn [response]
                      (info "clj-response:" response)
                      (let [{:keys [result error]} response]
                        (if error
                          (reject error)
                          (resolve result))))
           :error-handler (fn [err]
                            (error err)
                            (reject err))}))))))


