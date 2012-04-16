(defproject web/web "1.0.0-SNAPSHOT" 
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ring "1.1.0-beta2"]
;;                 [clj-stacktrace "0.2.4"]
                 [sandbar/sandbar "0.4.0-SNAPSHOT"]
                 [ring-basic-authentication "1.0.0"]
                 [clj-oauth2 "0.2.0"]
                 [clj-oauth "1.3.1-SNAPSHOT"]
                 [org.clojars.sritchie09/enlive "1.2.0-alpha1"]
                 [net.cgrand/moustache "1.1.0"]]
  :description "Samples with ring,moustache and enlive")
