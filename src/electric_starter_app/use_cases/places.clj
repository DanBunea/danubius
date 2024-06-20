(ns electric-starter-app.use-cases.places
  (:require
   [electric-starter-app.adapters.datomic.repo :as repo]))

(defn find-all-places [instant] (repo/find-all-places instant))

(comment

  (find-all-places nil)
  (find-all-places #inst "2024-06-20T13:43:04.953-00:00")

  nil)