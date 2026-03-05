(ns clj-service.permission
  (:require
   [modular.permission.user :refer [get-user]]))

(defn calc-user-roles [user]
  (if user
    (if-let [roles (:roles user)]
      roles
      #{})
    nil))

(defn calc-service-role [service]
  (:permission service))

(defn authorized? [service user]
  (let [required-role (calc-service-role service)
        user-roles (calc-user-roles user)]
    (cond
      (nil? required-role)
      true  ; if the service does not require anything, then authorized.

      (not user)
      false

      :else
      (contains? user-roles required-role))))
