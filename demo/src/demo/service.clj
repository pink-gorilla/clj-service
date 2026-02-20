(ns demo.service)

(defn add [a b]
  (+ a b))

(defn quote []
  "The early fish catches the worm.")

(defn quote-slow []
  (Thread/sleep 10000)
  "Born to be wild.")

(defn ex []
  (throw (Exception. "something bad happened")))

