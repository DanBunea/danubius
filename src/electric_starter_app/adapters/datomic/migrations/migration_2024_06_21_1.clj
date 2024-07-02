(ns electric-starter-app.adapters.datomic.migrations.migration-2024-06-21-1)

(def migration {:migration-name "initial-schema"
                :tx-data [{:db/ident :user/id
                           :db/cardinality :db.cardinality/one
                           :db/unique :db.unique/identity
                           :db/valueType :db.type/string}

                          {:db/ident :user/name
                           :db/cardinality :db.cardinality/one
                           :db/valueType :db.type/string}

                          {:db/ident :tx/user-id
                           :db/cardinality :db.cardinality/one
                           :db/valueType :db.type/ref}

                          {:db/ident :place/name
                           :db/cardinality :db.cardinality/one
                           :db/valueType :db.type/string}]})