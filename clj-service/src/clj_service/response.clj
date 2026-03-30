(ns clj-service.response
  (:require
   [taoensso.timbre :refer [debug debugf info infof warn error]]))

(defn stack-frame
  "Return a map describing the stack frame."
  [^StackTraceElement frame]
  {:class  (.getClassName frame)
   :method (.getMethodName frame)
   :file   (.getFileName frame)
   :line   (.getLineNumber frame)})

(defn stacktrace [e]
  (->> e
       .getStackTrace
       (map stack-frame)
       (into [])))

(defn convert-ex [ex]
  {:message (.getMessage ex)
   :class  (if-let [c (class ex)]
             (.getName c)
             "no class")
   :data (ex-data ex)
   :stacktrace (stacktrace ex)})

(defn response-error [ex]
  (let [response {:error (convert-ex ex)}]
    response))

(defn response-success [r]
  (let [response {:result r}]
    response))