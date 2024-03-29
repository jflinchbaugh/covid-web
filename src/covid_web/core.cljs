(ns covid-web.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [clojure.string :as s]
   [cljs-http.client :as http]
   [cljs.core.async :as async]
   [goog.string :as gstr]))

;; -------------------------
;; State

(defonce index-data (r/atom {}))

(defonce place-data (r/atom nil))

(defn resource-url [resource-name]
  (str "https://www.hjsoft.com/~john/covid/" resource-name))

(defn load-index-data! []
  (async/go (let [response (async/<! (http/get (resource-url "index.json")
                                               {:with-credentials? false}))]
              (reset! index-data (:body response)))))

(defn load-place-data! [file-name]
  (async/go (let [response (async/<! (http/get (resource-url file-name)
                                               {:with-credentials? false}))]
              (reset!
                place-data
                (assoc (:body response) :file-name file-name)))))

(defn max-deaths [place]
    (get-in place [:visualization :deaths :upper-outlier-threshold]))

(defn max-cases [place]
    (get-in place [:visualization :cases :upper-outlier-threshold]))

(defn any-neg? [day]
  (some neg? ((juxt :death-change :case-change) day)))

;; -------------------------
;; Components

(defn click-handler [file-name]
  (fn [] (if
          (or (nil? @place-data) (not= file-name (:file-name @place-data)))
           (load-place-data! file-name)
           (reset! place-data nil))))

(defn graph-length [space max-val val]
  (int (if (zero? val) 0 (* space (/ val max-val)))))

(defn graph-bar [max-val val]
  [:meter {:min 0 :max max-val :high max-val :value val}])

(defn place-table [place-data]
  [:table.place
   [:thead
    [:tr
     [:th.date "Date"]
     [:th.death-change "Deaths"]
     [:th.death-graph "Deaths"]
     [:th.case-change "Cases"]
     [:th.case-graph "Cases"]]]
   [:tbody
    [:tr
     [:td.date "Total"]
     [:td.death-change (:total-deaths place-data)]
     [:td.death-graph ""]
     [:td.case-change (:total-cases place-data)]
     [:td.case-graph ""]]
    (doall
      (for [day (:days place-data)]
        [(if (any-neg? day) :tr.negative :tr)
         {:key (:date day)}
         [:td.date (:date day)]
         [:td.death-change (:death-change day)]
         [:td.death-graph
          (graph-bar
            (max-deaths place-data)
            (:death-change-history day))]
         [:td.case-change (:case-change day)]
         [:td.case-graph
          (graph-bar
            (max-cases place-data)
            (:case-change-history day))]]))]])

(defn places-list []
  [:ul
   (doall
    (for [place (:places @index-data)]
      (let [selected? (= (:title @place-data) (:place place))]
        [:li {:key (:file-name place) :class (if selected? "current" "plain")}
         [:a
          {:on-click (click-handler (:file-name place))}
          (gstr/unescapeEntities (if selected? "&#x25BE;&nbsp;" "&#x25B8;&nbsp;"))
          (:place place)]
         (when selected?
           [:div
            [:div.prepared (:prepared @place-data)]
            [place-table @place-data]])])))])

;; -------------------------
;; Views

(defn places-page []
  [:div [:h1 (:title @index-data)]
   [places-list]])

(defn home-page []
  (places-page))
;; -------------------------
;; Initialize app


(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (load-index-data!)
  (mount-root))
