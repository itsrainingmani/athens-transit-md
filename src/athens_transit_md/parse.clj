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

(def dsdb (atom (read-transit-file)))

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

(defn walk-str
  "Four spaces per depth level."
  [depth node]
  (let [{:block/keys [string children]} node
        left-offset   (apply str (repeat depth "    "))
        walk-children (apply str (map #(walk-str (inc depth) %) children))]
    (str left-offset "- " string "\n" walk-children)))

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

(defn export-to-md
  [uid]
  (let [eid (e-by-av :block/uid uid)
        block (get-block-document eid)
        title (:node/title block)
        block-children (:block/children block)]
    (prn (str title " - Exporting page to markdown"))
    (->> block-children
         (map #(walk-str 0 %))
         (apply str))))

(defn export-all-pages
  []
  (let [all-pages (get-all-pages)
        all-page-uids (map :block/uid all-pages)
        all-md (map export-to-md all-page-uids)]
    (prn all-md)))
