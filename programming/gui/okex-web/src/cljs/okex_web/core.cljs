(ns okex-web.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [day8.re-frame.http-fx]
   [okex-web.events :as events]
   [okex-web.views :as views]
   [okex-web.config :as config]
   [okex-web.utils :refer [>evt <sub]]
   [okex-web.subs :as subs]
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
  (re-frame/dispatch [::events/fetch-instruments])
  (mount-root))
