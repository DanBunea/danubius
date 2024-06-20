(ns electric-starter-app.use-cases.place
  (:require
   [electric-starter-app.adapters.datomic.repo :as repo]))

(defn find-place [id] (repo/find-place id))

(comment

  (find-place 83562883711055)
  (find-place -83562883711055)

  nil)