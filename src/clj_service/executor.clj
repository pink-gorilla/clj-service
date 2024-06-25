(ns clj-service.executor
  (:require
   [de.otto.nom.core :as nom]
   [modular.permission.core :refer [user-authorized?]]))

;; USER

(defonce ^:dynamic
  ^{:doc "The user-id for which a clj-service gets executed"}
  *user* nil)

(defn execute [this permission-service user-id fun-symbol & args]
  (if-let [fun (get this fun-symbol)]
    (let [{:keys [function _permission fixed-args]} fun
          full-args (concat fixed-args args)]
      (if (user-authorized? permission-service fun-symbol user-id)
        (try {:result (apply function full-args)}
             (catch clojure.lang.ExceptionInfo e
               (nom/fail ::exception {:fun fun-symbol
                                      :error (pr-str e)
                                      :message (str "exception when executing function " fun-symbol)}))
             (catch AssertionError  e
               (nom/fail ::assert-error {:fun fun-symbol
                                         :error (pr-str e)
                                         :message (str "assert error when executing function " fun-symbol)}))
             (catch Exception e
               (nom/fail ::exception {:fun fun-symbol
                                      :error (pr-str e)
                                      :message (str "exception when executing function " fun-symbol)})))
        (nom/fail ::no-permission {:fun fun-symbol
                                   :user user-id
                                   :message (str "user-id  " user-id " is not authorized for: " fun-symbol)})))
    (nom/fail ::function-not-found {:fun fun-symbol
                                    :message "No service defined for this symbol."})))

