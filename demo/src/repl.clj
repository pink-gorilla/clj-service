(ns demo.repo
  (:require
   [modular.system]
   [missionary.core :as m]
   [clj-service.executor :refer [execute execute-wrapped]]))

(def ctx (modular.system/system :ctx))


ctx

(m/? (execute
      ctx
      {:user :florian
       :session :sleepy-hollow}
      {:fun 'demo.fortune-cookie/get-cookie}))


(m/? (execute-wrapped
      ctx
      {:user :florian
       :session :sleepy-hollow}
      {:fun 'demo.sleepy/happy?}))

(m/? (execute-wrapped
      ctx
      {:user :florian
       :session :sleepy-hollow}
      {:fun 'demo.sleepy/ex-test}))


(m/? (execute-wrapped
      ctx
      {:user :florian
       :session :sleepy-hollow}
      {:fun 'demo.sleepy/assert-test}))



