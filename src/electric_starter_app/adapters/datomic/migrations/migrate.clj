(ns electric-starter-app.adapters.datomic.migrations.migrate
  (:require
   [io.rkn.conformity :as c]))

(def schema
  {:yeller/migration-2024-06-21-12-29-32-UTC electric-starter-app.adapters.datomic.migrations.migration-2024-06-21-1/migration
   :yeller/migration-2024-06-21-12-29-33-UTC electric-starter-app.adapters.datomic.migrations.migration-2024-06-21-2/migration})

(defn load-schema [connection]
  (c/ensure-conforms connection schema))