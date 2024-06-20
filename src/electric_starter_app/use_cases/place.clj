(ns electric-starter-app.use-cases.place
  (:require
   [datomic.client.api :as dd]
   [electric-starter-app.adapters.datomic.conn :refer [get-db]]))

(defn find-place [id]
  (->>
   (dd/q
    '[:find (pull ?db-id [:db/id :place/name])
      :in $ ?db-id
      :where [?db-id :place/name]]
    (get-db)
    id)
   first
   first))

(comment

  (find-places 83562883711055)
  (find-places -83562883711055)

  nil)