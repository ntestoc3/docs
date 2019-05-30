(ns okex-web.subs
  (:require
   [re-frame.core :as re-frame]
   [com.rpl.specter :as s :refer-macros [select transform]]
   ))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::instruments
 (fn [db]
   (:instruments db)))

(re-frame/reg-sub
 ::depth-data
 (fn [db]
   (:depth-data db)))

(re-frame/reg-sub
 ::base-coins
 (fn [db]
   (->> (:instruments db)
        (select [s/ALL :base_currency])
        set
        sort)))

(re-frame/reg-sub
 ::quote-coins
 (fn [db [_ base-coin]]
   (println base-coin)
   (->> (:instruments db)
        (select [s/ALL #(= (:base_currency %) base-coin) :quote_currency])
        set
        sort)))
