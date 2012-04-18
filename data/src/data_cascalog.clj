(ns data-cascalog
  (:use data-core)
  (:require [cascalog.api :as ca]
            [cascalog.ops :as co]
            [clojure.data.json :as json]
            [clj-http.client :as http]))

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
   (ca/??<- ["?person" ?gender "?age"]
            (person "?person" "?gender" "?age")
            (< "?age" age)))

;; TODO explain !! ungrounding variable (outer-join ) ! nullable variable ? the rest
(defn count-followed
  "Returns name , age and count of persons followed"
  []
  (ca/??<- [?name ?age ?count]
           (person "?name" "?gender" "?age")
           (follows "?name" "!!followed")
           (co/!count !!followed :> ?count)))

(defn count-followed2
  "Same as count-followed but with construct"
  []
  (let [q (ca/construct ["?name" "?age" "?count"]
                        [[person  "?name" "?gender" "?age"]
                         [follows "?name" "!followed"]
                         [co/!count "!followed" :> "?count"]])]
    (ca/??- q)))


;;examples from https://github.com/nathanmarz/cascalog-conj/blob/master/src/clj/cascalog/conj/tunisia.clj

;;make dynamic buckets 
(defn build-bucketize [buckets]
  (fn bucketize[count]
  (->> buckets
       (partition 2 1)
       (filter (fn [[low up]]
                 (or (not up)
                     (and
                      (<= low count)
                      (< count up)))))
       first
       first)))

(comment
  "Since functions aren't serializable, Cascalog uses the var name to
communicate to the M/R tasks which function to execute. This is why
you need to use the var when inserting a function dynamically into the
query.

The reason you don't need to use the var name normally when using <-
is because <- is a macro and will resolve the var at compile-time.

The def*ops, on the other hand, do some trickery underneath so that
you can pass them around without having to resolve the var yourself.
It's a small advantage of a defmapop over a function.

-Nathan ")

;; Not working : Functions must have vars associated with them.
;; TODO #'bucketize doesn't work : Unable to resolve var: bucketize in this context
(defn count-by-age-wrong [buckets]
  (let [bucketize (build-bucketize buckets)
        q (ca/<- [?bucket ?count]
                 (person  ?name ?gender ?age)
                 (bucketize ?age :> ?bucket)
                 (co/count ?count))]
    (ca/??- q)))

;; parametrized operator
(ca/defmapop [bucketize [buckets]] [value]
  (->> buckets
       (partition 2 1)
       (filter (fn [[low up]]
                 (or (not up)
                     (and
                      (<= low value)
                      (< value up)))))
       first
       first))

(defn count-by-age[buckets]
  (ca/?<- [?bucket ?count]
           (person  ?name ?gender ?age)
           ((bucketize buckets) ?age :> ?bucket)
           (co/count ?count)))


;;How to get all persons following a givent set of persons
(defn following[names]
  (let [q (ca/construct ["?name" "?age" "?count"]
                       [follows "?name" "!followed"]
                       [co/!count "!followed" :> "?count"]
                       (map (fn[n] ["!followed" :> n]) names))]
    (ca/??- q)))


(defn tweets
  "Request Twitter API to get some data"
  [keyword & [size]]
  (let [ r (http/request {:method :get :url "http://search.twitter.com/search.json"
                          :query-params {:q keyword :rpp 100 :include_entities "true"
                                         :result_type "mixed"}})]
    (:results (json/read-json (:body r)))))


;;predicate operators :> :< :>> :<< :#>
(comment 
(def stock-tap
  (hfs-delimited "/pathto/data"
                 :delimiter ","
                 :outfields ["?exchange" "?stock-sym" "?date" "?open" "?high" "?low" "?close" "?volume" "?adj"]
                 :classes [String String String Float Float Float Float Integer Float]
                 :skip-header? true))

(select-fields stock-tap ["?stock-sym" "?open"])

(co/all

(defn cond? [int] (< int 5))

(def m
  (<- [?a :> ?b]
      (inc ?a :> ?b)))

(def n
  (<- [?a :> ?b]
      (dec ?a :> ?b)))

(def query
  (let [pred (if cond? m n)]
;;  (let [pred (if cond? #'m #'n)]
    (?<- (stdout)
         [?a ?b]
         (integer ?a)
         (pred ?a :> ?b))))



)
