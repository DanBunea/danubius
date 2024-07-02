(ns electric-starter-app.adapters.datomic.migrations.migrations
  (:require [datomic.client.api :as d]
            [clojure.tools.logging :as log]
            [electric-starter-app.adapters.datomic.migrations.migration-2024-06-21-1 :as migration-2024-06-21-1]
            [electric-starter-app.adapters.datomic.migrations.migration-2024-06-21-2 :as migration-2024-06-21-2]))

(defn run-migration [conn migration]
  (let [{:keys [migration-name tx-data]} migration
        full-tx-data (cons {:migration/name migration-name} tx-data)
        tx-result (d/transact conn {:tx-data full-tx-data})]
    (log/info "Migration applied:" migration-name)
    tx-result))

(defn ensure-migration-schema [conn]
  (let [schema [{:db/ident :migration/name
                 :db/valueType :db.type/string
                 :db/cardinality :db.cardinality/one
                 :db/unique :db.unique/identity}]]
    (d/transact conn {:tx-data schema})))

(defn get-applied-migrations [db]
  (->> db (d/q '[:find ?name
                 :where [_ :migration/name ?name]])
       (map first)))

(defn apply-migrations [conn migrations]
  (let [db (d/db conn)
        applied (set (get-applied-migrations db))
        pending (remove #(applied %) migrations)]
    (doseq [migration pending]
      (run-migration conn migration))))

(def migrations
  [migration-2024-06-21-1/migration
   migration-2024-06-21-2/migration])

(defn run-migrations [conn]
  (ensure-migration-schema conn)
  (apply-migrations conn migrations))

