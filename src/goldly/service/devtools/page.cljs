(ns goldly.service.devtools.page
  (:require
   [promesa.core :as p]
   [reagent.core :as r]
   [goldly.service.core :refer [clj]]))

(defn kw-item [t]
  [:p.m-1 (pr-str t)])

(defn keyword-list [name list]
  [:div.mt-10
   [:h2.text-2xl.text-blue-700.bg-blue-300 name]
   (into [:div.grid.grid-cols-2.md:grid-cols-4]
         (map kw-item (sort list)))])

(defn clj-service-page [{:keys [_route-params _query-params _handler] :as _route}]
  (let [rp (clj 'clj-service.core/services-list)
        a (r/atom [])]
    (p/then rp (fn [r] (reset! a r)))
    (fn [_]
      [keyword-list "services" @a])))