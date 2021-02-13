(ns athens-transit-md.cli
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

;; read https://github.com/clojure/tools.cli
;; for further details on option parsing
(def cli-options
  [;; First three strings describe:
   ;;   1. short-option
   ;;   2. long-option with optional example argument description
   ;;   3. description
   ;; All three are optional and positional.
   ["-p" "--port PORT" "Port number"
    :default 80
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-H" "--hostname HOST" "Remote host"                    ; in real life, we would parse to an InetAddress
    :default (str "localhost")
    ;; Specify a string to output in the default column in the options summary
    ;; if the default value's string representation is very ugly
    :default-desc "localhost"]
    ;; If no required argument description is given, the option is assumed to
    ;; be a boolean option defaulting to nil
   [nil "--detach" "Detach from controlling process"]
   ["-v" nil "Verbosity level; may be specified multiple times to increase value"
    ;; If no long-option is specified, an option :id must be given
    :id :verbosity
    :default 0
    ;; Use assoc-fn to create non-idempotent options
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-f" "--file" "Transit File path"
    :default (str "index.transit")
    :default-desc "Transit file"]
   ["-h" "--help"]])

(defn usage [options-summary]
  (string/join
   \newline
   ["This is my program. There are many like it, but this one is mine."
    ""
    "Usage: athens-transit-md [options] action"
    ""
    "Options:"
    options-summary
    ""
    "Actions:"
    "  start    Start a new server"
    "  stop     Stop an existing server"
    "  status   Print a server's status"
    "  convert  Begin Conversion process"
    ""
    "Please refer to the manual page for more information."]))

(defn error-msg [errors]
  (str "Errors while parsing command line args:\n\n"
       (string/join \newline errors)))

(defn command-ok?
  "make sure the 'command' part of the command line is correct"
  [arguments]
  (and (= 1 (count arguments))
       (#{"start" "stop" "status" "convert"} (first arguments))))

(defn invalid-action [summary arguments]
  (str (usage summary)
       \newline \newline
       "Error: invalid action '" (first arguments) "' specified!"))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message or optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)
        cmd (first arguments)]
    (println "---- debug output, remove for production code ----")
    (println "options   " (pr-str options))
    (println "arguments " (pr-str arguments))
    (println "errors    " (pr-str errors))
    (println "summary   " \newline summary)
    (println "--------------------------------------------------")
    (cond
      (:help options)         {:exit-message (usage summary) :ok? true}  ; help => exit OK with usage summary
      errors                  {:exit-message (error-msg errors)}         ; errors => exit with description of errors
      (command-ok? arguments) {:action cmd :options options}             ; custom validation on arguments
      :else                   {:exit-message
                               (invalid-action summary arguments)})))    ; failed custom validation => exit with usage summary

(defn exit [status msg]
  (println msg)
  (System/exit status))

