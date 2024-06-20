(ns electric-starter-app.use-cases.users
  (:require
   [datomic.client.api :as dd]
   [electric-starter-app.adapters.datomic.conn :refer [get-db]]))

(defn find-all-users []
  (->> (get-db)
       (dd/q
        '[:find (pull ?db-id [:db/id :user/id :user/name])
          :where [?db-id :user/id]])
       (mapv first)))

(comment

  (find-all-users)

  nil)