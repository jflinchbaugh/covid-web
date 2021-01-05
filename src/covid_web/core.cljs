(ns covid-web.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [clojure.string :as s]
   [cljs-http.client :as http]
   [cljs.core.async :as async]))

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
              (reset! place-data (:body response)))))

;; -------------------------
;; Components

(defn click-handler [file-name]
  (fn [] (load-place-data! file-name)))

(defn places-list []
  [:ul
   (doall
    (for [place (:places @index-data)]
      [:li {:key (:file-name place)}
       [:a
        {:href "#" :on-click (click-handler (:file-name place))}
        (:place place)]]))])

(defn back-handler! []
  (reset! place-data nil))

;; -------------------------
;; Views

(defn graph-length [space max-val val]
  (if (zero? val) 0 (* space (/ val max-val))))

(defn graph-bar [ch space max-val val]
  [:div.bar (s/join (repeat (graph-length space max-val val) ch))])

(defn places-page []
  [:div [:h1 (:title @index-data)]
   (places-list)])

(defn place-page []
  [:div
   [:a {:href "#" :on-click back-handler!} "<< Back"]
   [:h1 (:title @place-data)]
   [:table
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
      [:td.death-change (:total-deaths @place-data)]
      [:td.death-graph ""]
      [:td.case-change (:total-cases @place-data)]
      [:td.case-graph ""]]
     (doall
      (for [day (:days @place-data)]
        [:tr {:key (:date day)}
         [:td.date (:date day)]
         [:td.death-change (:death-change day)]
         [:td.death-graph (graph-bar "!" 50 (:max-deaths @place-data) (:death-change day))]
         [:td.case-change (:case-change day)]
         [:td.case-graph (graph-bar "!" 75 (:max-cases @place-data) (:case-change day))]]))]]])

(defn home-page []
  (if @place-data
    (place-page)
    (places-page)))
;; -------------------------
;; Initialize app


(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (load-index-data!)
  (mount-root))
