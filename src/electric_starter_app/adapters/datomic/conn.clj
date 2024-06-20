(ns electric-starter-app.adapters.datomic.conn
  (:require
   [datomic.client.api :as d]
   [clojure.java.io :as io]))

(def current-directory (.getCanonicalPath (io/file ".")))
(def client (d/client {:server-type :dev-local
                       :storage-dir (str current-directory "/.datomic-local")
                       :system "danubius"}))

(def db-name "danubius")
(defn get-connection []
  (d/connect client {:db-name db-name}))

(defn get-db []
  (d/db (get-connection)))

(defn db [!conn]
  (d/db !conn))

(comment
  (d/delete-database client {:db-name db-name})
  (d/create-database client {:db-name db-name})
  nil)

(defn q [query & inputs]
  (apply d/q (into [] (concat [query] inputs))))

(defn pull
  ([db arg-map]
   (d/pull db arg-map))
  ([db selector eid]
   (d/pull db selector eid)))

(defn transact [!conn tx-data]
  (d/transact !conn {:tx-data tx-data}))
