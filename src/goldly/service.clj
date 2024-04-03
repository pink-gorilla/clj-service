(ns goldly.service
  (:require
   [taoensso.timbre :as timbre :refer [info]]
   [extension :refer [discover write-service get-extensions]]
   [goldly.service.expose :as expose]))

(defn- expose-extension-clj-services [clj-services]
  (doall
   (map expose/start-services clj-services)))

(defn- exts->services [exts]
  (->> (get-extensions exts {:clj-services nil})
       (map :clj-services)
       (remove nil?)))

(defn start-clj-services [exts]
  (info "starting clj-services ..")
  (let [config (exts->services exts)]
    (write-service exts :clj-services config)
    (expose-extension-clj-services config)))