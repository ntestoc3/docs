(ns okex-web.events
  (:require
   [re-frame.core :as re-frame]
   [okex-web.db :as db]
   [okex-web.utils :refer [evt-db2]]
   [ajax.core :as ajax]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

;; 保存所有币对信息
(evt-db2 :set-instruments [:instruments])

;;; ================ api 请求
(re-frame/reg-event-fx
 :fetch-instruments
 (fn-traced [_ _]
            {:http-xhrio {:method :get
                          :uri "https://www.okex.com/api/spot/v3/instruments"
                          :timeout 8000
                          :response-format  (ajax/json-response-format {:keywords? true})
                          :on-success [:set-instruments]
                          :on-failure [:fail-load [:fetch-instruments]]}}))
