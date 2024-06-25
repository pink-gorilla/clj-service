(ns funs)

(defn fun-noargs []
  27)

(defn fun-add [a b]
  (+ a b))

(defn fun-ex []
  (throw (Exception. "bad")))

(defn fun-service [this data]
  (println "fun-service: this: " this " data: " data)
  (concat this data))

