(ns demo.app
  (:require
   ;[frontend.css :refer [css-loader]]
   ;[frontend.notification :refer [notification-container]]
   ;[frontend.dialog :refer [modal-container]]
   [webly.spa.env :refer [get-resource-path]]))

(defn wrap-webly [page match]
  [:div
   ;[modal-container]
   ;[notification-container]
   ;[css-loader (get-resource-path)]
   [page match]])

(def routes
  [["/" {:name 'demo.page/service-page}]
   ["/help" {:name 'demo.admin/help-page}]])

