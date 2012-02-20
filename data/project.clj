(defproject query-survey "1.0.0-SNAPSHOT"
  :description "Compare various data APIs"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [korma "0.3.0-beta2"]
                 [mysql/mysql-connector-java "5.1.6"]
                 [congomongo "0.1.8"]
                 [cascalog "1.9.0-wip3"]]
  :dev-dependencies [ [lein-midje "1.0.7"]
                               [midje-cascalog "0.4.0"]
                              [org.apache.hadoop/hadoop-core "0.20.2-dev"]
                      ])
