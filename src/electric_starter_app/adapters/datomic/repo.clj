(ns electric-starter-app.adapters.datomic.repo
  (:require
   [datomic.client.api :as dd]
   [electric-starter-app.adapters.datomic.conn :refer [get-db get-connection]]))

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

  (find-place 83562883711055)
  (find-place -83562883711055)

  nil)

(defn find-all-places-transactions []
  (->> (get-db)
       (dd/q
        '[:find (pull ?t [*])
          :where [?db-id :place/name _ ?t]])
       (mapv first)))

(comment

  (find-all-places-transactions)

  nil)

(defn find-all-places
  [instant]
  (->> (if instant
         (dd/as-of (get-db) instant)
         (get-db))
       (dd/q
        '[:find (pull ?db-id [:db/id :place/name])
          :where [?db-id :place/name]])
       (mapv first)))

(comment

  (find-all-places nil)
  (find-all-places #inst "2024-06-20T13:43:04.953-00:00")

  nil)

(defn collect-exception [e]
  (let [ex (ex-data e)]
    (if (:cognitect.anomalies/category ex)
      {:message (:cognitect.anomalies/message ex)
       :type (:cognitect.anomalies/category ex)
       :errors (:db/errors ex [(:db/error ex)])}
      ;; 
      ex)))

(defn transact [tx-data user-id]
  (let [all-datoms (vec (conj tx-data
                              [:db/add "datomic.tx" :tx/user-id [:user/id user-id]]))

        _ (prn :83485 all-datoms)]

    (try
      (dd/transact (get-connection) {:tx-data all-datoms})
      (catch Exception e
        {:error (collect-exception e)}))))

(defn find-all-users []
  (->> (get-db)
       (dd/q
        '[:find (pull ?db-id [:db/id :user/id :user/name])
          :where [?db-id :user/id]])
       (mapv first)))

(comment

  (find-all-users)

  nil)