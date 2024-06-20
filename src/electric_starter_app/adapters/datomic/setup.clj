(ns electric-starter-app.adapters.datomic.setup
  (:require
   [electric-starter-app.adapters.datomic.conn :refer [get-connection transact]]
   [electric-starter-app.adapters.datomic.schema :refer [base-schema]]))

(defn setup-test-database [db-conn]
  (transact db-conn base-schema)

  (transact db-conn [{:db/id "u1"
                      :user/id "mrx"
                      :user/name "Mr X"}
                     {:db/id "u2"
                      :user/id "mrsy"
                      :user/name "Mrs Y"}

                     {:db/id "paris1"
                      :place/name "rue de rivoli, 101, Paris, France"}]))



(comment 
  
  (setup-test-database (get-connection))
  
  )
