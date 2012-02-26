(ns base
  (:use [net.cgrand.enlive-html :as html]
        [ring.adapter.jetty :only [run-jetty]]
        [ring.middleware params keyword-params]
        [ring.middleware.file :only [wrap-file]]        
        [net.cgrand.moustache :only [app delegate]]))


(defn simple-handler [text]
  (fn [request]
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (str text)
     }))

(html/defsnippet section "main.html" [[:.span4 (html/nth-of-type 1)]]
  [title]
  [:h2] (html/content title)
  )

(html/deftemplate index "main.html" [items]
  ;;make stylesheet path absolute
  [[:link (html/attr= :rel "stylesheet")]] (fn[node]
                                             (update-in node [:attrs :href] (partial str "/")))
  ;;insert one section per item
  [:.container :.row] (html/content (map section items)))

(def routes1
  (app ["first" & ] (simple-handler "first page")
       ["second" &] (simple-handler (apply str (index ["clojure" "java" "perl"])))
       [ &] (simple-handler "Page not defined")
       ))

(def main-routes
  (app (wrap-file "resources")
;;       (wrap-params)
;;       (wrap-keyword-params)
       routes1
       ))

(defn start [ & [port & options]]
  (run-jetty (var main-routes) {:port (or port 8080) :join? false}))

