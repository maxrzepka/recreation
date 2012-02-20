(ns data-core)

(def person
  ;; [person gender age]
  [
 ["alice" "f" 28]
 ["bob" "m" 33]
 ["chris" "m" 40]
 ["david" "m" 25]
 ["emily" "f" 25]
 ["george" "m" 31]
 ["gary" "m" 28]
 ["harold" "m" 27]
 ["luanne" "f" 36]
 ])
 
(def follows
  [
   ;; [person-follower person-followed]
   ["alice" "david"]
   ["alice" "bob"]
   ["alice" "emily"]
   ["bob" "david"]
   ["bob" "george"]
   ["bob" "luanne"]
   ["david" "alice"]
   ["david" "luanne"]
   ["emily" "alice"]
   ["emily" "bob"]
   ["emily" "george"]
   ["emily" "gary"]
   ["george" "gary"]
   ["harold" "bob"]
   ["luanne" "harold"]
   ["luanne" "gary"]
   ])

(def person-follows
  (let [followsByPerson (reduce (fn[m [k v]] (update-in m [k] conj v)) {} follows)]
    (for[ [n g a] person] {:name n :gender g :age a :follows (followsByPerson n)})))



