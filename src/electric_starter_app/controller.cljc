(ns electric-starter-app.controller
  (:require
   [hyperfiddle.rcf :refer [tests]]
   [electric-starter-app.hierarchical-state-machine :refer [transition]]))

(defn dispatch-to-machine [machine !atom [event-name event-params]]
  (prn ::dispatch-to-machine event-name event-params)
  (->> (transition machine @!atom  [event-name event-params])
       (reset! !atom)))