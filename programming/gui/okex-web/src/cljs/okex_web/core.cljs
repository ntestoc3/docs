(ns okex-web.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [day8.re-frame.http-fx]
   [okex-web.errors]
   [okex-web.loadding]
   [okex-web.utils :refer [>evt <sub]]
   [okex-web.events :as events]
   [okex-web.views :as views]
   [okex-web.config :as config]
   ))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
