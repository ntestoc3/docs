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
 :<- [::instruments]
 (fn [instruments]
   (-> (select [s/ALL :base-currency] instruments)
       set
       sort)))

(re-frame/reg-sub
 ::quote-coins
 (fn [db]
   (:quote-coins db)))

(re-frame/reg-sub
 ::base-coin
 (fn [db]
   (:base-coin db)))

(re-frame/reg-sub
 ::quote-coin
 (fn [db]
   (:quote-coin db)))

(re-frame/reg-sub
 ::error
 (fn [db]
   (:error db)))
