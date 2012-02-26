(defproject web "1.0.0-SNAPSHOT"
  :description "Samples with ring,moustache and enlive"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ring "1.0.1"]
                 [clj-stacktrace "0.2.4"] ;;upgrade from 0.2.2 to 0.2.4 in order to work with swank-clojure
                 [org.clojars.sritchie09/enlive "1.2.0-alpha1"]
                 [net.cgrand/moustache "1.1.0"]])
