(ns data-cascalog
  (use data-core)
  (require [cascalog.api :as ca]
           [cascalog.ops :as co]))

;; A query starts with an output [ ?person ?a2 ] and the rest are predicates
;; A variable can be symbol ?a2 or string "?a2" , when starting with ! is nullable
;; Types of predicates :
;;   - generator ( source of data ) like (person "?person" "?gender" "?age")
;;   - operation like (< "?age" 30) 
;;   - aggregator like  (co/!count !!followed :> ?count)
;;
;; predicate without output variables (< ?age 30) act as a filter

(defn person-younger-than
   [age]
   (ca/??<- ["?person" "?gender" "?age"]
            (person "?person" "?gender" "?age")
            (< "?age" age))

(defn count-followed
  "Returns name , age and count of persons followed"
  []
  (ca/??<- [?name ?age ?count]
           (person "?name" "?gender" "?age")
           (follows "?name" "!!followed")
           (co/!count !!followed :> ?count)))


;; TODO how to make it more dynamic ?
;; describe the query as plain map (cf mongodb) and coerce map to a cascalog query

