(ns data-cascalog
  (use data-core)
  (require [cascalog.api :as ca]
           [cascalog.ops :as co]))

;; (
(defn query1
   []
   (ca/??<- ["?person" "?a2"]
            (person "?person" "?gender" "?age")
            (< "?age" 30)
            (* 2 "?age" :> "?a2")))

(defn query2
  "Returns person following more than n persons, uses !! for outer join"
  [n]
  (ca/??<- [?name ?count]
           (person "?name" "?gender" "?age")
           (follows "?name" "!!followed")
           (co/!count !!followed :> ?count)
           (> "?count" n)))


