(ns clj-service.permission
  (:require
   [modular.permission.role :as role]))

(defn calc-user-roles [user]
  ; todo move to permission.
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
    (role/authorized-roles? required-role user-roles)))
