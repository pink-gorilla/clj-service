(ns clj-service.response
  (:require
   [taoensso.timbre :refer [debug debugf info infof warn error]]))

(defn response-error [err data]
  (let [response {:error err
                  :data data}]
    (error "clj-service response-error error: " response)
    response))

(defn response-success [r]
  (let [response {:result r}]
    (error "clj-service response-success: " response)
    response))