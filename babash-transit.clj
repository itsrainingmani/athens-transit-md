#!/usr/bin/bb

(ns athens.transit
  (:require
    [datascript.core :as d]
    [datascript.db   :as db]
    [cognitect.transit :as t])
  (:import
    [datascript.db DB Datom]
    ;; [me.tonsky.persistent_sorted_set PersistentSortedSet]
    [java.io ByteArrayInputStream ByteArrayOutputStream]))


(def read-handlers
  { "datascript/DB"    (t/read-handler db/db-from-reader)
    "datascript/Datom" (t/read-handler db/datom-from-reader) })


;; (def write-handlers
;;   { DB    (t/write-handler "datascript/DB"
;;             (fn [db]
;;               { :schema (:schema db)
;;                 :datoms (:eavt db) }))
;;     Datom (t/write-handler "datascript/Datom"
;;             (fn [^Datom d]
;;               (if (db/datom-added d)
;;                 [(.-e d) (.-a d) (.-v d) (db/datom-tx d)]
;;                 [(.-e d) (.-a d) (.-v d) (db/datom-tx d) false])))
;;     PersistentSortedSet (get t/default-write-handlers java.util.List) })


(defn read-transit [is]
  (t/read (t/reader is :json { :handlers read-handlers })))


(defn read-transit-str [^String s]
  (read-transit (ByteArrayInputStream. (.getBytes s "UTF-8"))))


;; (defn write-transit [o os]
;;   (t/write (t/writer os :json { :handlers write-handlers }) o))


;; (defn write-transit-bytes ^bytes [o]
;;   (let [os (ByteArrayOutputStream.)]
;;     (write-transit o os)
;;     (.toByteArray os)))
    

;; (defn write-transit-str [o]
;;   (String. (write-transit-bytes o) "UTF-8"))

;; (let [transit-file (slurp "index.transit")
;;       in (ByteArrayInputStream. (.getBytes transit-file "UTF-8"))
;;       reader (t/reader in :json)]
;;   (prn (t/read reader)))

(def INDEX_FILE "index.transit")

(defn read-transit-file
  "Reads the index.transit file from the path given"
  ([]
   (read-transit-file INDEX_FILE))
  ([index-file-path]
   (let [transit-file (slurp index-file-path)
         dt-db (read-transit-str transit-file)]
     dt-db)))

(defn get-all-pages
  "Gets the datoms for all the pages in the DB"
  [db]
  (let [query '[:find ?t
                :where [?t :node/title]]]
    (->> db
         (d/q query)
         (reduce (fn [acc v] (conj acc (first v))) '[])
         (d/pull-many db '[*]))))

(prn (get-all-pages (read-transit-file)))
