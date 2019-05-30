(ns okex-web.events
  (:require
   [re-frame.core :as re-frame]
   [okex-web.db :as db]
   [okex-web.utils :refer [evt-db2]]
   [ajax.core :as ajax]
   [goog.string :as gstring]
   [goog.string.format]
   [com.rpl.specter :as s :refer-macros [select transform]]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   ))

(defn format-depth-data
  "格式化深度数据"
  [data]
  (transform [(s/multi-path :asks :bids) s/INDEXED-VALS]
             (fn [[idx [price amount order-count]]]
               [idx {:pos idx
                     :price price
                     :amount amount
                     :order-count order-count}])
             data))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

;; 保存所有币对信息
(evt-db2 :set-instruments [:instruments])

(re-frame/reg-event-db
 :set-depth-data
 (fn-traced [db [_ data]]
            (->> (format-depth-data data)
                 (assoc db :depth-data))))

;;; ================ api 请求
(re-frame/reg-event-fx
 ::fetch-instruments
 (fn-traced [_ _]
            {:http-xhrio {:method :get
                          :uri "https://www.okex.com/api/spot/v3/instruments"
                          :timeout 8000
                          :response-format (ajax/json-response-format {:keywords? true})
                          :on-success [:set-instruments]
                          :on-failure [:fail-load [:fetch-instruments]]}}))

(re-frame/reg-event-fx
 ::fetch-depth-data
 (fn-traced [_ [_ instrument-id]]
            {:http-xhrio {:method :get
                          :uri (gstring/format "https://www.okex.com/api/spot/v3/instruments/%s/book" instrument-id)
                          :timeout 8000
                          :response-format (ajax/json-response-format {:keywords? true})
                          :on-success [:set-depth-data]
                          :on-failure [:fail-load [:fetch-depth-data]]}}))
