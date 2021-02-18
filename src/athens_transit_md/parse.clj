(ns athens-transit-md.parse
  (:require [athens-transit-md.db :as db]
            [datascript.transit :as dt]
            [clojure.java.io :as io])
  (:gen-class))


(defn touch-output-folder
  ([]
   (let [folder-name (str "./athens-" (db/now-format))]
     (touch-output-folder folder-name)))
  ([folder-name]
   (let [folder-exists? (.exists (io/file folder-name))]
     (if-not folder-exists?
       (.mkdir (io/file folder-name))
       (prn "The folder already exists")))))

(defn read-transit-file
  "Reads the index.transit file from the path given and loads it into the dsdb atom"
  ([]
   (read-transit-file db/INDEX_FILE))
  ([index-file-path]
   (let [transit-file (slurp index-file-path)
         dt-db (dt/read-transit-str transit-file)]
     (reset! db/dsdb dt-db))))


(defn walk-str
  "Four spaces per depth level."
  [depth node]
  (let [{:block/keys [string children]} node
        left-offset   (apply str (repeat depth "    "))
        walk-children (apply str (map #(walk-str (inc depth) %) children))]
    (str left-offset "- " string "\n" walk-children)))


(defn convert-to-md
  [uid]
  (let [eid (db/e-by-av :block/uid uid)
        block (db/get-block-document eid)
        block-children (:block/children block)]
    (->> block-children
         (map #(walk-str 0 %))
         (apply str))))

(defn write-to-file
  [title content]
  (spit title content))

(defn construct-file-path
  [title o]
  (str (.getPath (io/file o title)) ".md"))

(defn convert-all-pages
  [o]
  (let [all-pages (db/get-all-pages)
        all-page-map (map #(select-keys % [:node/title :block/uid]) all-pages)
        all-page-fixed (map #(update % :node/title construct-file-path o) all-page-map)]
    (touch-output-folder o)  ;; Create the Output Folder
    (->> all-page-fixed
         (map (fn [{node-title :node/title
                    block-uid  :block/uid}]
                (prn (str "Writing to " node-title))
                (write-to-file node-title (convert-to-md block-uid)))))))
