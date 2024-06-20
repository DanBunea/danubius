(ns electric-starter-app.use-cases.places-transactions
  (:require
   [electric-starter-app.adapters.datomic.repo :as repo]))

(defn find-all-places-transactions [] (repo/find-all-places-transactions))

(comment

  (find-all-places-transactions)

  nil)