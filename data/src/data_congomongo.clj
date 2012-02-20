(ns data-congomongo
  (use data-core
       [somnium.congomongo.config :only [*mongo-config*]])
  (require [somnium.congomongo :as m]))

(defn bootstrap-mongo []
  (let [mongo-url "mongodb://localhost:27017/test"]
    (when (not (m/connection? *mongo-config*))
      (println "Initializing mongo @ " mongo-url)
      (m/mongo! :db "test" :host "localhost" :port 27017 ))))

(defn init-data
  "Create a collection persons and insert person as  (no join in mongodb)"
  []
  (m/create-collection! :persons)
  (m/mass-insert! :persons person-follows))

(defn simple-query []
  (m/fetch :persons :where {:age 28}))
           