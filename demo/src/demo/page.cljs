(ns demo.page
  (:require
   [reagent.core :as r]
   [promesa.core :as p]
   [goldly.service.core :refer [clj]]))

(def state
  (r/atom {}))

(defn clj-add []
  (run-a state [:add-result]  'demo.service/add 2 7))

(defn clj-quote []
  (run-a state [:quote] 'demo.service/quote))

(defn clj-ex []
  (run-a state [:ex-result] 'demo.service/ex))

(defn clj-quote-slow []
  (run-a-map {:a state
              :path [:quote-slow]
              :fun 'demo.service/quote-slow
              :timeout 1000}))

(defn clj-promise []
  (swap! state assoc :quote-promise (clj 'demo.service/quote)))

(defn promise-status [pr]
  [:div "promise status:"
   " resolved?: " (pr-str (p/resolved? pr))
    ; " rejected?: " (pr-str (p/rejected? pr))
   " pending?: " (pr-str (p/pending? pr))
   " done?: " (pr-str (p/done? pr))
   " promise?: " (pr-str (p/promise? pr))
   " thenable?: " (pr-str (p/thenable? pr))])

(defn promise-result [pr]
  (if (p/resolved? pr)
    [:p (pr-str @pr)]
    [:p "-pending"]))

(defn show-page []
  [:div
   [:p.text-big.text-blue-900.text-bold "clj-services tests .."]

   [:div.bg-green-500.m-5.p-5
    [:button.bg-blue-500 {:on-click #(clj-add)} "add numbers (clj)"]
    [:p "result: " (pr-str (:add-result @state))]]

   [:div.bg-green-500.m-5.p-5
    [:button.bg-blue-500 {:on-click #(clj-quote)} "get quote (fast)"]
    [:p "result: " (pr-str (:quote @state))]]

   [:div.bg-green-500.m-5.p-5
    [:button.bg-blue-500 {:on-click #(clj-quote-slow)} "get quote (slow)"]
    [:p "result: " (pr-str (:quote-slow @state))]]

   [:div.bg-green-500.m-5.p-5
    [:button.bg-blue-500 {:on-click #(clj-promise)} "get quote (promise)"]
    (when (:quote-promise @state)
      [:p "promise status: " [promise-status (:quote-promise @state)]]
      [:p "result: " [promise-result (:quote-promise @state)]])]

   [:div.bg-blue-300.mg-3
    "state: "
    (pr-str @state)]])

(defn service-page [_route]
  [show-page])

