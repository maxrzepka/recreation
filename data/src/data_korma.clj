(ns data-korma
  (require [data-core :as c]
           [korma.db :as kd]
           [korma.core :as k]))

(defn bootstrap
  "Connect to a database with the following tables :
 create table person (name varchar(30) , gender varchar(2) , age int(11));
 create table follow (follower varchar(30) not null, followed varchar(30) not null);
For MySQL set specific delimiters
"
  []
  ;;Database
  (kd/defdb mys (kd/mysql {:host "localhost" :db "test" :user "test" :password "test" :delimiters "`"}))
  ;;Entity definitions
  (k/defentity follows (k/database mys) (k/table :follow) (k/entity-fields :follower :followed))

  (k/defentity persons (k/database mys) (k/pk :name)
    (k/table :person) 
    (k/entity-fields :gender :age )
    (k/has-many follows {:fk :follower})))

;; use of defonce for

(defn init-data
  "Populates tables follow person"
  []
  (k/insert persons (k/values (for[[n g a] c/person] {:name n :gender g :age a})))
  (k/insert follows (k/values (for[[from to] c/follows] {:follower from :followed to})))
  )

(defn group-query[]
  (k/select follows
            (k/fields :follower)
            (k/aggregate (count 1) :cnt :follower)))

;;
;; Error : cannot join !!
;;
(defn join-query[]
  (k/select persons
            (k/with follows)
            (k/fields :name :age)
            (k/aggregate (count 1) :cnt :name)))



