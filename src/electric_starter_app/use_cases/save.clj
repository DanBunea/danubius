(ns electric-starter-app.use-cases.save
  (:require
   [electric-starter-app.adapters.datomic.repo :as repo]))

(defn transact [tx-data user-id] (repo/transact tx-data user-id))