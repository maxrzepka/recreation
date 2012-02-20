(ns data-cascalog
  (use data-core)
  (require [cascalog.api :as ca]
           [cascalog.ops :as co]))

;; (simple-query  (hfs-textline "/tmp/results"))
(defn query1
  "Returns results of a simple query
   out = :dir result saved in folder by default /tmp/results"
  [out & more]
  (let [output ({:dir (ca/hfs-textline (if (seq more) (first more) "/tmp/results"))} out (ca/stdout))]
       (ca/?<- output
               ["?person" "?a2"]
               (person "?person" "?gender" "?age")
               (< "?age" 30)
               (* 2 "?age" :> "?a2"))))

(comment
  "Set up to run cascalog from emacs REPL : copy from cascalog.playgroung "
  (import '[java.io PrintStream]
          '[cascalog WriterOutputStream]
          '[org.apache.log4j Level Logger WriterAppender SimpleLayout])

  "TODO how to change log level"
  (-> (Logger/getRootLogger)
;      (.setLevel (. Level FATAL))
      (.addAppender (WriterAppender. (SimpleLayout.) *out*)))
;      (.setLevel (. Level DEBUG)))
  (System/setOut (PrintStream. (WriterOutputStream. *out*)))
)

