(ns data-korma
  (require [data-core :as c]
           [clojure.java.jdbc :as sql]
           [korma.db :as kd]
           [korma.core :as k]))

;;  "Connect to a database with the following tables :
;; create table person (name varchar(30) , gender varchar(2) , age int(11));
;;  create table follow (follower varchar(30) not null, followed varchar(30) not null);
  
(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "mysql"
         :subname "//localhost:3306/test"
         :user "test"
         :password "test"})

(defn create-wishlist []
  (sql/with-connection db
    (sql/create-table :wishlist
      [:code :varchar "PRIMARY KEY"]
      [:title :varchar "NOT NULL"]
      [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"])))

(defn create-wish []
  (sql/with-connection db
    (sql/create-table :wish
      [:id :serial "PRIMARY KEY"]
      [:description :varchar "NOT NULL"]
      [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]
      [:url :varchar]
      [:wishlist_code :varchar]
      ["constraint fk_wish_wishlist foreign key(wishlist_code) 
        references wishlist(code) on delete cascade"])))

(defn drop-tables []
  (sql/with-connection db
    (sql/drop-table :wish)
    (sql/drop-table :wishlist)))

(k/defentity wish)
(k/defentity wishlist
  (k/pk :code)
  (k/has-many wish {:fk "wishlist_code"}))

(defn read-wishlist [code]
  "Selects one wishlist row and all corresponding wish rows for a given code."
  (first
    (k/select wishlist (k/fields :id :url :description)
      (k/with wish 
        (k/fields :code :title)
        (k/order :id :asc))
      (k/where {:code code}))))


;; Database
;; For MySQL set specific delimiters 
(kd/defdb mys
  (kd/mysql {:host "localhost" :db "test" :user "test" :password "test" :delimiters "`"}))

;; Entity definitions
(k/defentity follows (k/database mys) (k/table :follow) (k/entity-fields :follower :followed))

(k/defentity persons (k/database mys) (k/pk :name)
    (k/table :person) 
    (k/entity-fields :gender :age )
    (k/has-many follows {:fk :follower}))


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
;; How to join tables ?
;; (k/sql-only (join-query))
;; "SELECT `person`.`name`, `person`.`age`, COUNT(?) `cnt` FROM `person` GROUP BY `person`.`follower`"
;;
(defn join-query[]
  (k/select persons
            (k/with follows)
            (k/fields :name :age)
            (k/aggregate (count 1) :cnt :follower)))


(comment 
(-> (select* models/album)
  (fields [:album.id :album_id]
          :name
          :lots-of-other-fields)
  (join models/album-artist (= :album.id :album_artist.album))
  (join models/artist (= :artist.id :album_artist.artist)))
) 

