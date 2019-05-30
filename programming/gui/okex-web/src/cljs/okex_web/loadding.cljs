(ns okex-web.loadding
  (:require [re-frame.core :as rf]
            [day8.re-frame.tracing :refer-macros [fn-traced]]))

;;; 提供Loading事件支持
;;; :status [path] 订阅
;;; :begin-load [path] next-value 事件 添加新的loading
;;; :succeed-load [path] 设置loading完成
;;; :fail-load [path] msg 设置loading失败，并设置错误消息msg

(defn begin-load [state next-value]
  (cond
    (nil? state) ;; consider nil a stable value
    {:status :loading
     :value nil
     :next-value next-value}

    (= :stable (:status state))
    {:status :loading
     :value (:value state)
     :next-value next-value}

    (= :loading (:status state))
    (assoc state :next-value next-value)))

(defn succeed-load [state]
  (cond
    (nil? state)
    state

    (= :stable (:status state))
    state

    (= :loading (:status state))
    {:status :stable
     :value (:next-value state)}))

(defn fail-load [state]
  (cond
    (nil? state)
    state

    (= :stable (:status state))
    state

    (= :loading (:status state))
    {:status :stable
     :value (:value state)}))

(def loading-db-path [::loading])

(defn ->loading-path
  [path]
  (vec (concat loading-db-path path)))

(defn get-status
  "从db中获得path的状态,
  主要供event使用"
  [db path]
  (get-in db (->loading-path path)))

(rf/reg-sub
 ::loadings
 (fn [db]
   (get-in db loading-db-path)))

(rf/reg-sub
 :status
 :<- [::loadings]
 (fn-traced [res-loading [_ path]]
            (get-in res-loading path)))

(rf/reg-event-db
 :begin-load
 (fn-traced [db [_ path next-value]]
            (update-in db (->loading-path path) begin-load next-value)))

(rf/reg-event-db
 :succeed-load
 (fn-traced [db [_ path]]
            (update-in db (->loading-path path) succeed-load)))

(rf/reg-event-fx
 :fail-load
 (fn-traced [{:keys [db]} [_ path msg]]
            {:db (update-in db (->loading-path path) fail-load)
             :dispatch [:set-error path msg]}))

