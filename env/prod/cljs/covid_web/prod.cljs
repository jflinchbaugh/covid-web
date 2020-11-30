(ns covid-web.prod
  (:require
    [covid-web.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
