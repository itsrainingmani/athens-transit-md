(ns athens-transit-md.parse
  (:require [datascript.transit :as dt]
            [datascript.core :as d])
  (:gen-class))

(def INDEX_FILE "index.transit")

(defn read-transit-file
  "Reads the index.transit file from the path given"
  ([]
   (read-transit-file INDEX_FILE))
  ([index-file-path]
   (let [transit-file (slurp index-file-path)
         dt-db (dt/read-transit-str transit-file)]
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
