(ns electric-starter-app.signals
  (:require
   [hyperfiddle.rcf :refer [tests]]))

(def dynamic-signal-vars (atom {}))

(defn signal [signal-name]
  (let [ss (symbol signal-name)
        existing (ns-resolve *ns* ss)]
    (when-not existing
      (intern *ns* ss (atom 0)))
    (let [dyn-var (-> (ns-resolve *ns* ss)
                    (deref))]
      (swap! dynamic-signal-vars assoc signal-name (System/currentTimeMillis))
      dyn-var)))

(defn remove-signal [signal-name]
  (let [ss (symbol signal-name)
        existing (ns-resolve *ns* ss)]
    (when existing
      (do
        (ns-unmap *ns* ss)
        (swap! dynamic-signal-vars dissoc signal-name)))))

(tests
  (def signal-name (str "topic-" (rand-int 1000)))
  (def signal-atom (signal signal-name))
  (type signal-atom) := clojure.lang.Atom
  @signal-atom := 0
  (swap! signal-atom inc)
  @signal-atom := 1

  (nil? (ns-resolve *ns* (symbol signal-name))) := false

  (remove-signal signal-name)
  (nil? (ns-resolve *ns* (symbol signal-name))) := true)

(defn remove-unused-signals-after-23h
  "Checks and removes unused vars not accessed within the last 24 hours"
  []
  (let [current-time (System/currentTimeMillis)
        twenty_four_hours (* 1000 60 60 24) ;; 24 hours in milliseconds
        vars (deref dynamic-signal-vars)]
    (doseq [[var-name last-accessed] vars]
      (when (> (- current-time last-accessed) twenty_four_hours)
        (remove-signal var-name)))))

(comment
  @dynamic-signal-vars

  nil)
