(ns clj-service.browser-id
  (:require
   [taoensso.timbre :refer [error info]]
   ;[ring.util.response :as response]
   [ring.middleware.cookies :as cookies]
   [human-id.core :refer [human-id]]))

(def cookie-name "browser-id")

(defn- bare-session-request
  [request]
  (let [existing-browser-id  (get-in request [:cookies cookie-name :value])]
    (info "COOKIES: " (:cookies request))
    (info "browser-id: " existing-browser-id)
    (merge request {:browser-id existing-browser-id})))

(defn session-request
  [request]
  (-> request
      cookies/cookies-request
      (bare-session-request)))

(defn- bare-session-response
  [response request]
  (if-let [existing-browser-id (get-in request [:cookies cookie-name :value])]
    (do (info "response existing-browser-id: " existing-browser-id)
        response)
    (let [new-browser-id (human-id)
          cookie {cookie-name {:value new-browser-id
                               :max-age (* 60 60 24 365 20) ;; 20 years
                                             ;:path "/"
                                             ;:http-only true
                                             ;:secure true
                                             ;:same-site :strict
                               }}]
      (assoc response :cookies (merge (response :cookies) cookie)))))

(defn session-response
  [response request]
  (when response
    (let [new-response (-> response
                           (bare-session-response request)
                           cookies/cookies-response)]

      (info "new response: " new-response)
      new-response)))

(defn wrap-browser-id [handler]
  (fn
    ([request]
     (let [request (session-request request)]
       (-> (handler request)
           (session-response request))))
    ([request respond raise]
     (let [request (session-request request)]
       (handler request
                (fn [response]
                  (respond (session-response response request)))
                raise)))))

(def browser-id-middleware
  {:name ::browser-id-session
   :compile
   (fn [_route-data _router-opts]
     (fn [handler]
       (wrap-browser-id handler)))})