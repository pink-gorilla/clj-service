(ns goldly.service
  (:require
   [taoensso.timbre :as timbre :refer [info]]
   [extension :refer [write-target-webly get-extensions]]
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
    (write-target-webly :clj-services config)
    (expose-extension-clj-services config)))