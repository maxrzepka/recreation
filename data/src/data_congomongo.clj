(ns data-congomongo
  (:use data-core
       [somnium.congomongo.config :only [*mongo-config*]])
  (:require [somnium.congomongo :as m]))

;;  mongodb daemon should run  
;; 
(defn bootstrap-mongo []
  (let [mongo-url "mongodb://localhost:27017/test"]
    (when (not (m/connection? *mongo-config*))
      (println "Initializing mongo @ " mongo-url)
      (m/mongo! :db "test" :host "localhost" :port 27017 ))))

(defn init-data
  "Creates a collection named persons and inserts one record per person (no join in mongodb)
    {:name \"emily\" :gender \"f\" :age 25 :follows [\"gary\" \"george\" \"bob\" \"alice\"] }
"
  []
  (m/mass-insert! :persons person-follows))

(defn query1
  "Simple query with where and only clauses"
  []
  (m/fetch :persons :where {:age {:$gt 28}} :only [:name]))

(defn query2
  " db.persons.find({follows: {$size:  2 ,$in: [\"gary\"]}} ); "
  []
  (m/fetch :persons :where { :follows {:$size 2 :$in ["gary"]}}))

;;
;; How to get any person following more than 2 persons ?
;; These 2 queries are incorrects
;;   db.persons.find({$where: "this.follows.length > 2" } );
;;    db.persons.find({follows: {$size:  {:$gt 2}}});
