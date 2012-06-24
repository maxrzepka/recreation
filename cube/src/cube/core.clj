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

;;TODO re-factor can be dependent of the data-set
(defn agg-values [agg]
  (condp = agg
    :type types
    :country (keys countries)
    :region (distinct (vals countries))
    nil))

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

(defn compute-queries
  "returns all possible queries, dim map all values of each dimension"
  [data]
  (into #{}
        (for [a [#{:type} #{:region} #{:country} "all" nil #{:type :region}
                     #{:type :country} #{:region :country}  #{:type :region :country} ]
                  t (into #{nil} (map :type data))
                  r (into #{nil} (map :region data))
                  c (into #{nil} (map :country data))
              :when (or (and (nil? r) (nil? c)) (= r (countries c)))]
              {:aggregate a
               :filter {:region r :type t :country c}}
              )))

;; Query constraint : aggregate is a
;;
(defn valid-query? [q]
  (let [aggregate (:aggregate q)]
    (or (nil? aggregate) (set? aggregate))))

(def fields [:type :region :country :acct :balance :commission])

(def aggregates [:type :region :country])

(defn keyword->casvar [k]
  (str "?" (name k)))

(defn data->generator [data]
  (let [
        rows (vec (map (apply juxt fields) data))]
    (list* rows (map #(str "?" (name %)) fields))))

(defn to-cascalog [query]
  (let [raw (vec (map keyword->casvar fields))
        sums ["?total" "?tbalance" "?tcommission"]
        agg (:aggregate query)
        outvars (if agg
                  (concat (if (seq agg)
                            (mapv keyword->casvar (filter agg aggregates)))
                          sums)
                  raw)
        filters (keep (fn [[k v]] (when v [#'= (keyword->casvar k) v])) (:filter query))
        agg (when agg
              [[co/!count "?acct" :> "?total"]
               [co/sum "?balance" :> "?tbalance"]
               [co/sum "?commission" :> "?tcommission"]])]
    (vector outvars (vec (filter (comp not nil?) (concat agg filters))))
    ;;[outvars agg filters]
    ))


(defn kw->title [k]
  (-> k name clojure.string/capitalize))

(defn to-set [s]
  (if (sequential? s) (into #{} s) #{s}))

(defn to-seq [coll]
  (if (sequential? coll) (seq coll) (list coll)))

(defn q->header [query]
  (if (:aggregate query)
    (let [agg (:aggregate query)]
     [(keep (fn [a] (when (agg a) (kw->title a))) aggregates)
      ["Acct #" "Balance" "Commission"]])
    (map kw->title fields)))

(defn execute [query data]
  (let [[outvars preds] (to-cascalog query)
        preds (vec (cons (data->generator data) preds))
        q (ca/construct outvars preds)]
;;TODO rearranged result set : nested colls (hierarchy) when aggregate query
    {:header (q->header query)
     :query query
     :rows (first (ca/??- q))}))

;;TODO How to distinguish full aggregation #{} and no aggregation nil
;; ?? contraint {:post [(set? (:aggregate q)]}
(defn toggle-aggregate [q agg]
  {:post [(valid-query? %)]}
  (update-in q [:aggregate]
               (fnil (if (agg (:aggregate q)) disj conj) #{})
               agg))

(defn possible-values1 [q agg]
  (to-seq (agg q (agg-values (agg (:aggregate q))))))

(defn aggregate-values [data]
  (reduce (fn [a r]
            (reduce #(update-in %1 [%2] conj (%2 r)) a aggregates))
          (into {} (map vector aggregates (repeat #{})))
          data))

(defn possible-values
  "Returns function giving the possible values of a aggregate
  if query provided restrict to it :
     values inside filter if existing
     or only for aggregate"
  [data]
  (let [all-values
        (if data
          (aggregate-values data)
          agg-values)]
    (fn [agg & [q]]
      (let [vls (seq
        (if q
          (agg (:filter q)
               (if (or (not (:aggregate q))  ;;agreggate
                       (-> q :aggregate agg)) ;;
                 (all-values agg)))
          (all-values agg)))]
        (if vls vls [nil])))))

(defn filter-values
  "Returns a map : combination of aggregates => all values as a map found in data
For eaxmple [:type] => #{ {:type \"A\"} {:type \"B\"} }
"
  [data]
  (let [ks (conj (distinct (for [a aggregates b aggregates]
                             (filterv (into #{} [a b]) aggregates)))
                 aggregates)
        init-map (reduce (fn [m k] (assoc m k #{})) {} ks)]
    (reduce (fn [m r]
              (reduce (fn [m1 k] (update-in m1 [k] conj (select-keys r k)))
                      m ks))
            init-map
            data)))

(defn next-queries
  "returns all queries reachable with one step
remove , add , change 1 parameter of the query
"
  ([query] (next-queries query nil) ;;obsolete
     )
  ([query data]
     (let [agg-values (filter-values data)]
         (concat
       ;;aggregate changes
       (map (partial toggle-aggregate query) aggregates)
       ;;filter changes
       (for [agg aggregates
             v (map agg (agg-values [agg]))
             :when (not (= (find (:filter query) agg) [agg v]))]
         (update-in query [:filter] assoc agg v))
       ;;raw queries if aggregates
       (when-let [agg (:aggregate query)]
         (if (and (set? agg) (seq agg))
           (map #(assoc {} :filter %) (agg-values (filterv agg aggregates)))
           {}))))))

(defn all-queries [data & queries]
  (loop [facets #{}
         queries (if (seq queries)
                   (into #{} queries)
                   #{{:aggregate #{}} {:aggregate (into #{} aggregates)}})
         size 0]
    (let [queries (remove facets queries)
          facets (reduce (fn [fs q] (conj fs q))
                         facets queries)
          queries (into #{} (mapcat #(next-queries % data) queries))]
     (if (or (= size (count facets)) (> (count facets) 200))
       facets
       (recur facets queries (count facets))))))

#_(defn load-facets [data & queries]
  (loop [facets {} queries (if (seq queries) (into #{} queries) #{{:aggregate #{}}})]
    (let [queries (remove facets queries)
          facets (reduce (fn [fs q] (assoc fs (.hashCode q) (execute q data)))
                         facets queries)
          queries (into #{} (mapcat next-queries queries))]
     (if (> (count facets) 3)
       facets
       (recur facets queries)))))

