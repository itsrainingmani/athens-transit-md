(ns athens-transit-md.db
  (:require [datascript.core :as d])
  (:gen-class))


(def INDEX_FILE "index.transit") ;; Default path to the index.transit file
(def FOLDER_LOC "./athens-")     ;; Default Output Folder Location
(def dsdb (atom #{}))            ;; dsdb is initially an empty atom map


(defn now-format
  []
  (.format (java.text.SimpleDateFormat. "yyyyMMdd-HHmmss") (new java.util.Date)))

(defn e-by-av
  [a v]
  (-> (d/datoms @dsdb :avet a v) first :e))


(defn get-all-pages
  "Gets the datoms for all the pages in the DB"
  []
  (let [query '[:find ?t
                :where [?t :node/title]]]
    (->> @dsdb
         (d/q query)
         (reduce (fn [acc v] (conj acc (first v))) '[])
         (d/pull-many @dsdb '[*]))))


(defn sort-block-children
  [block]
  (if-let [children (seq (:block/children block))]
    (assoc block :block/children
           (vec (sort-by :block/order (map sort-block-children children))))
    block))


(def block-document-pull-vector
  '[:db/id :block/uid :block/string :block/open :block/order {:block/children ...} :block/refs :block/_refs])


(defn get-block-document
  [id]
  (->> (d/pull @dsdb block-document-pull-vector id)
       (sort-block-children)))
