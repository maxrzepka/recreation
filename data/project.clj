(defproject query-survey/query-survey "1.0.0-SNAPSHOT" 
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [korma "0.3.0-beta2"]
                 [mysql/mysql-connector-java "5.1.6"]
                 [congomongo "0.1.8"]
                 [clj-http "0.2.5"]
                 [org.clojure/data.json "0.1.2"]
                 [cascalog "1.9.0-wip8"]]
  :profiles {:dev
             {:dependencies
              [[midje-cascalog "0.4.0"]
               [org.apache.hadoop/hadoop-core "0.20.2-dev"]]}}
  :min-lein-version "2.0.0"
  :plugins [[lein-midje "1.0.7"]]
  :description "Compare various data APIs")
