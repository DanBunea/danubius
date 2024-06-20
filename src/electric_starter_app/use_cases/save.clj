(ns electric-starter-app.use-cases.save
  (:require
   [electric-starter-app.adapters.datomic.repo :as repo]
   [electric-starter-app.signals :refer [signal]]))

(defn transact [tx-data user-id]
  (let [result (repo/transact tx-data user-id)]
    (swap! (signal "topic-places") inc)
    result))