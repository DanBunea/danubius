(ns electric-starter-app.use-cases.places
  (:require
   [datomic.client.api :as dd]
   [electric-starter-app.adapters.datomic.conn :refer [get-db]]))

(defn find-all-places []
  (->> (get-db)
       (dd/q
        '[:find (pull ?db-id [:db/id :place/name])
          :where [?db-id :place/name]])
       (mapv first)))

(comment

  (find-all-places)

  nil)