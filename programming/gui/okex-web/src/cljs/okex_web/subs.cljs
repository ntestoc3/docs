(ns okex-web.subs
  (:require
   [re-frame.core :as re-frame]
   [com.rpl.specter :as s :refer-macros [select transform]]
   ))

;; 标题，懒得改名字了
(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

;; 币对信息
(re-frame/reg-sub
 ::instruments
 (fn [db]
   (:instruments db)))

;; 深度数据
(re-frame/reg-sub
 ::depth-data
 (fn [db]
   (:depth-data db)))

;; 注意base-coins是基于instruments更新的，不能通过直接访问db的方式获取base-coins,
;; 否则instruments刷新，base-coins的订阅不会自动刷新。
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

;; 错误信息
(re-frame/reg-sub
 ::error
 (fn [db]
   (:error db)))
