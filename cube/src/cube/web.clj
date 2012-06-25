(ns cube.web
  (:use [cube.core])
  (:require [net.cgrand.enlive-html :as h]))


(defmacro maybe-do [pred action] `(if ~pred ~action identity))
#_(defmacro maybe-content
  ([expr] `(if-let [x# ~expr] (html/content x#) identity))
  ([expr & exprs] `(maybe-content (or ~expr ~@exprs))))


(h/defsnippet th-model "cube.html"
  [[:div.row-fluid (h/nth-of-type 1)] :table :thead :tr [:th h/first-child]]
  [{:keys [title type]}]
  [h/root] (h/do->
            (h/content title)))

(h/defsnippet td-model "cube.html"
  [[:div.row-fluid (h/nth-of-type 1)] :table :tbody :tr [:td h/first-child]]
  [{:keys [value type]}]
  [h/root] (h/do->
            (h/content (if (= java.lang.Double (class value))
                         (format "%.0f" value)
                         (str value)))))

(h/defsnippet row-model "cube.html"
  [[:div.row-fluid (h/nth-of-type 1)] :table :tbody [:tr h/first-child]]
  [row & [code]]
  [h/root] (h/do-> (h/set-attr :facet code) ;;add facet id
            (h/content (map (fn [v] (td-model {:value v})) row))))

(h/defsnippet agg-model "cube.html"
  [[:div.row-fluid (h/nth-of-type 1)] [:div.btn-toolbar (h/nth-child 1)]  [:a h/first-child]]
  [{:keys [title active code]}]
  [h/root] (h/do-> (if active (h/add-class "btn-primary") (h/remove-class "btn-primary"))
                   (h/set-attr :onclick (str "goto(this,'" code "')"))
                   (h/content title)))

(h/defsnippet filter-model "cube.html"
  [[:div.row-fluid (h/nth-of-type 1)] [:div.btn-toolbar (h/nth-child 2)]  [:a h/first-child]]
  [{:keys [title active]}]
  [h/root] (h/do-> (if active (h/add-class "btn-primary") (h/remove-class "btn-primary"))
                   (h/content title)))

;;TODO how to remove Aggregate and filter panels when raw queries
(h/defsnippet facet-html "cube.html" [:#content [:div.row-fluid (h/nth-of-type 1)]]
  [{header :header rows :rows query :query}]
  [h/root] (h/set-attr :id (.hashCode query))
  ;;Aggregate buttons
  [[:div.btn-toolbar h/first-child] :div.btn-group]
  (h/content (map #(agg-model  {:title (kw->title %)
                                :active (-> query :aggregate %)
                                :code (.hashCode (toggle-aggregate query %))})
                  aggregates))
  ;;Filters
  [[:div.btn-toolbar (h/nth-child 2)] :div.btn-group]
  (h/content "Coming...")
  ;;(h/content (map #(filter-model  {:title (kw->title %)}) aggregates))
  ;;table
  ;;with macro
  ;;[:table] (maybe-do (agg-query? query) (h/add-class "selectable"))
  ;;with closure
  ;;[:table] #(if (agg-query? query) ((h/add-class "selectable") %) (identity %))
  ;;with plain code
  [:table] (if (agg-query? query) (h/add-class "table-hover") identity)
  [:table :thead :tr]
  (h/content (map #(th-model {:title %}) (flatten header)))
  [:table :tbody]
  (h/content (map #(row-model % (if-let [q (details-query % query)] (.hashCode q))) rows)))

;;template with all facets
(h/deftemplate main "cube.html" [facets]
  [:#content] (h/content (map facet-html facets)))

(defn export [file dataset]
  ((spit file
         )))