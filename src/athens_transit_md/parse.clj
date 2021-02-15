(ns athens-transit-md.parse
  (:require [athens-transit-md.db :as db]
            [datascript.transit :as dt])
  (:gen-class))


(defn read-transit-file
  "Reads the index.transit file from the path given and loads it into the dsdb atom"
  ([]
   (read-transit-file db/INDEX_FILE))
  ([index-file-path]
   (let [transit-file (slurp index-file-path)
         dt-db (dt/read-transit-str transit-file)]
     (reset! db/dsdb dt-db))))


(defn now [] (new java.util.Date))


(defn walk-str
  "Four spaces per depth level."
  [depth node]
  (let [{:block/keys [string children]} node
        left-offset   (apply str (repeat depth "    "))
        walk-children (apply str (map #(walk-str (inc depth) %) children))]
    (str left-offset "- " string "\n" walk-children)))


(defn export-to-md
  [uid]
  (let [eid (db/e-by-av :block/uid uid)
        block (db/get-block-document eid)
        title (:node/title block)
        block-children (:block/children block)]
    (prn (str title " - Exporting page to markdown"))
    (->> block-children
         (map #(walk-str 0 %))
         (apply str))))


(defn export-all-pages
  []
  (let [all-pages (db/get-all-pages)
        all-page-uids (map :block/uid all-pages)
        all-md (map export-to-md all-page-uids)]
    (prn all-md)))
