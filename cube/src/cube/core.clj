(ns cube.core
  (:require [cascalog.api :as ca]
            [cascalog.ops :as co]))

(def types ["A" "B" "C" "D"])

(def countries {"France" "Europe"
                   "UK" "Europe"
                   "Germany" "Europe"
                   "USA" "America"
                   "Canada" "America"
                   "China" "Asia"
                   "India" "Asia"})

(defn generate-dataset [size]
 (map (fn [i]
           ((fn [m] (assoc m :region (countries (:country m))))
            {:type (get types (rand-int (count types)))
                :country (-> (seq countries)
                             (nth (rand-int (count countries)))
                             first)
                :acct (str "A" i)
                :balance (rand-int 10000)
                :commission (* 0.01 (rand-int 1000))
                }))
         (range 1 (inc size))))

(defn queries
  "returns all possible queries, dim map all values of each dimension"
  [data]
  (into #{}
        (for [a [:type :region :country "all" nil [:type :region]
                     [:type :country] [:region :country] [:type :region :country]]
                  t (into #{nil} (map :type data))
                  r (into #{nil} (map :region data))
                  c (into #{nil} (map :country data))
              :when (or (and (nil? r) (nil? c)) (= r (countries c)))]
              {:aggregate a
               :filter {:region r :type t :country c}}
              )))

(defn data->pred [data]
  (let [fields [:type :country :region :acct :balance :commission]
        rows (map (apply juxt fields) data)]
    (list* rows (map #(str "?" (name %)) fields))))

(defn to-cascalog [query]
  (ca/construct ))

(defn execute [query data]
  (let [q (ca/construct )]))

(defn dataset->str [coll]
  )


;;working
(def q ( ca/construct ["?region"] [(data->pred d)] ))
;;not working
(def q (ca/construct ["?region" "?total"] ;["?region" "?total" "?tbalance" "?tcommission"]
                     [ (data->pred d)
                      [co/count "?acct" :> "?total"]
;;                      (co/sum "?balance" :> "?tbalance")
;;                      (co/sum "?commission" :> "?tcommission")
                      ]
                     ))


(def q (ca/construct ["?region" "?total"]
                     [ [ [["Europe" "A"] ["America" "B"] ] "?region" "?acct"]
                       [co/!count "?acct" :> "?total"] ]))

(ca/??<- [?region ?total]
         ([["Europe" "A"] ["Americ" "B"] ] ?region ?acct)
         (co/!count ?acct :> ?total))