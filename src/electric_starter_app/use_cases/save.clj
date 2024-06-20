(ns electric-starter-app.use-cases.save
  (:require
   [datomic.client.api :as dd]
   [electric-starter-app.adapters.datomic.conn :refer [get-connection]]))

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