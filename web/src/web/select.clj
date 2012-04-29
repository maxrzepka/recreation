(ns web.select
  (:use [ring.adapter.jetty :only [run-jetty]]
        [ring.middleware.file :only [wrap-file]]
        [ring.middleware.params :only [wrap-params]]
        [ring.middleware.keyword-params :only [wrap-keyword-params]]
        [ring.middleware.file :only [wrap-file]]
        [ring.middleware.stacktrace :only [wrap-stacktrace ]]
        [ring.util.response :only [response file-response redirect]]
        [net.cgrand.moustache :only [app delegate]])
  (:require [net.cgrand.enlive-html :as h]))


;;TODO how to handle compile error
;;TODO how to inject full namespace in enlive function :
;;         content --> net.cgrand.enlive-html/content
(defn str->code
  [s]
  (if (string? s)
    (try (load-string s)
         (catch Throwable t {:message "" :error (.getMessage t)}))
    s))

(defn code->str [c]
  (cond
    (string? c) c
    (nil? c) ""
    :else (pr-str c)))

(defn nodes->str [nodes]
  (cond
    (string? nodes) nodes
    :else (apply str (h/emit* (h/flatten-nodes-coll nodes)))))

(defn select-nodes
  ""
  [selector text]
;;  (println "select-nodes--> " selector)
  (nodes->str (h/select (h/html-snippet text)
                       (str->code selector))))

;;
;; How to load enlive functions
;;
(defn transform-nodes
  [selector trans text]
  (nodes->str
   (h/transform (h/html-snippet text)
                (str->code selector)
                (str->code trans))))
;;
(def examples
  [{:id "simple" :title "Simple" :selector [:a]
    :source "<span><a>llll</a></span>"}
   {:id "fragment" :title "Fragment" :selector "{[:h1] [:p]}"
    :source "<div><h1>title</h1><h2>Sub title</h2><p>some text</p></div><h1>Another Title</h1>"}
   {:id "tagattr" :title "Tag/Attr"
    :selector  "[[:a (net.cgrand.enlive-html/attr= :href \"/\")]]"
    :source "<div>><a>ll1</a></li><li><a href=\"/\">index</a></div>"}
   ]
  )

;; misc middleware
(defn haz? [coll element] (boolean (some (conj #{} element) coll)))
;;use pr-str instead of json-pr
(defn wrap-logging
  "Ring middleware for request logging.
   Why JSON: http://journal.paul.querna.org/articles/2011/12/26/log-for-machines-in-json/
   Options:
    :output -- :stdout, :stderr, a string (file path) or a function (for log systems)
               :stdout is default
    :status-filter -- a collection of status codes to log responses with or a predicate
                      (eg. #(> % 399)). nil is default
    :keys-filter -- which keys of (merge req res) to log"
  [handler {:keys [output status-filter keys-filter]
            :or {output :stdout
                 status-filter nil
                 keys-filter [:status :uri :remote-addr :request-method]}}]
  (fn [req]
    (let [res (handler req)
          status-filter (cond
                          (nil? status-filter) (fn [s] true)
                          (coll? status-filter) #(haz? status-filter %)
                          (fn? status-filter) status-filter)
          logger (cond
                   (= output :stdout) println
                   (= output :stderr) #(binding [*out* *err*]
                                         (println %))
                   (string? output) #(spit output (str % "\n") :append true)
                   (fn? output) output)
          entry (-> (select-keys (merge req res) keys-filter)
                    pr-str
                    (.replace "\\" ""))]
      (if (status-filter (:status res)) (logger entry))
      res)))


;; misc enlive utils
(defn render [t]
  (apply str t))

(def render-to-response
  (comp response render))

;; from https://$github.com/swannodette/enlive-tutorial
(defn render-request [afn & args]
  (fn [req] (render-to-response (apply afn args))))

(defn prepend-attrs [att prefix]
  (fn[node] (update-in node [:attrs att] (fn[v] (str prefix v)))))

(defmacro mydeftemplate
  "Same as deftemplate but make resources url absolute ( prepend / )"
  [name source args & forms]
  `(h/deftemplate ~name ~source ~args
     [[:link (h/attr= :rel "stylesheet")]] (prepend-attrs :href "/")
     ~@forms))

;;

(h/defsnippet nav-item "select.html" [:#navexamples [:li (h/nth-of-type 2)]]
  [{:keys [title id]}]
  [:a] (h/do-> (h/set-attr :href (str "/" id))
               (h/content title)))

(mydeftemplate index "select.html" [{:keys [source selector selection]}]
               [:#navexamples] (h/content (mapcat nav-item examples))
               [:#i_selector] (h/set-attr :value (code->str selector))
               [:#i_source] (h/content source)
               [:#l_selector] (h/content (code->str selector))
               [:#l_selection] (h/content selection)
               )

(defn find-example [id]
  (first (filter #(= id (:id %)) examples)))

(defn append-selection [{:keys [selector transform source] :as params}]
  (assoc params
    :selection
    (if transform
      (transform-nodes selector transform source)
      (select-nodes selector source))))

(defn process-selection [{params :params :as req}]
;;  (println "process-selection--> " (pr-str params))
  (render-to-response (index (append-selection params))))

(def routes
  (app (wrap-file "resources")
       (wrap-params)
       (wrap-keyword-params)
       (wrap-stacktrace)
       (wrap-logging {:keys-filter [:status :uri :params :request-method]})
       [ id & ] {:get (render-request index (append-selection (find-example id)))
                 :post process-selection}
       ))

(defn start [ & [port & options]]
  (run-jetty (var routes) {:port (or port 8080) :join? false}))

(defn -main []
  (let [port (try (Integer/parseInt (System/getenv "PORT"))
                  (catch  Throwable t 8080))]
    (start port)))



