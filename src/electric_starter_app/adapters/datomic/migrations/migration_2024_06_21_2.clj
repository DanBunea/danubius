(ns electric-starter-app.adapters.datomic.migrations.migration-2024-06-21-2)

(def migration [{:db/id "u1"
                 :user/id "mrx"
                 :user/name "Mr X"}
                {:db/id "u2"
                 :user/id "mrsy"
                 :user/name "Mrs Y"}
               
                {:db/id "paris1"
                 :place/name "rue de rivoli, 101, Paris, France"}])