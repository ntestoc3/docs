(ns okex-web.errors
  (:require [re-frame.core :as rf]
            [day8.re-frame.tracing :refer-macros [fn-traced]]))


;;; 提供错误事件支持
;;; :error [path] 订阅错误消息
;;; :set-error [path] message 设置错误消息
;;; :clear-error [path] 清除[path]下的错误消息

(def errors-db-path [::errors])

(defn ->errors-path
  [path]
  (vec (concat errors-db-path path)))

(rf/reg-sub
 ::errors
 (fn [db]
   (get-in db errors-db-path)))

(rf/reg-sub
 :error
 :<- [::errors]
 (fn-traced [errors [_ path]]
            (get-in errors path)))

(rf/reg-event-db
 :set-error
 (fn-traced [db [_ path msg]]
            (assoc-in db (->errors-path path) msg)))

(rf/reg-event-db
 :clear-error
 (fn-traced [db [_ path]]
            (assoc-in db (->errors-path path) nil)))


