(ns okex-web.events
  (:require
   [re-frame.core :as re-frame]
   [okex-web.db :as db]
   [okex-web.utils :refer [evt-db2]]
   [ajax.core :as ajax]
   [goog.string :as gstring]
   [goog.string.format]
   [camel-snake-kebab.core :as csk]
   [com.rpl.specter :as s :refer-macros [select select-one transform]]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   ))

;;;;;;;;;;;;;;;;;;;;;;; helper functions
(defn format-map-keys
  "把map的keyword转换为clojure格式"
  [m]
  (s/transform [s/ALL s/MAP-KEYS] csk/->kebab-case-keyword m))

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

(defn get-instrument-id
  "获得当前币对名称"
  [db]
  (let [base-coin (:base-coin db)
        quote-coin (:quote-coin db)]
    (s/select-one [s/ALL
                   #(and (= (:base-currency %) base-coin)
                         (= (:quote-currency %) quote-coin))
                   :instrument-id]
                  (:instruments db))))

(defn get-quote-coins
  [db base-coin]
  (->> (:instruments db)
       (select [s/ALL #(= (:base-currency %) base-coin) :quote-currency])
       set
       sort))

;;;;;;;;;;;;;;;;;;;;;;;;; timer event

(defn dispatch-timer-event
  []
  (let [now (js/Date.)]
    (re-frame/dispatch [:timer now])))  ;; <-- dispatch used

;; 200毫秒刷新1次
(defonce do-timer (js/setInterval dispatch-timer-event 200))

;;;;;;;;;;;;;;;;;;;;;;; event db
(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

;; 设置标题
(evt-db2 :set-name [:name])

;; 保存所有币对信息
(re-frame/reg-event-db
 :set-instruments
 (fn-traced [db [_ data]]
            (->> (format-map-keys data)
                 (assoc db :instruments))))

(evt-db2 :set-quote-coins [:quote-coins])

(evt-db2 :set-quote-coin [:quote-coin])

(re-frame/reg-event-db
 :set-depth-data
 (fn-traced [db [_ data]]
            (->> (format-depth-data data)
                 (assoc db :depth-data))))

(re-frame/reg-event-db
 :set-base-coin
 (fn-traced [db [_ base-coin]]
            (re-frame/dispatch [:set-quote-coins (get-quote-coins db base-coin)])
            (assoc db :base-coin base-coin)))

;; 保存错误信息
(re-frame/reg-event-db
 :error
 (fn-traced [db [_ path error]]
            (assoc db :error {:path path
                              :msg error})))

(re-frame/reg-event-db
 :clear-error
 (fn-traced [db _]
            (assoc db :error nil)))

;;; ================ api 请求
(re-frame/reg-event-fx
 ::fetch-instruments
 (fn-traced [_ _]
            {:dispatch [:clear-error]
             :http-xhrio {:method :get
                          :uri "https://www.okex.com/api/spot/v3/instruments"
                          :timeout 8000
                          :response-format (ajax/json-response-format {:keywords? true})
                          :on-success [:set-instruments]
                          :on-failure [:error [:fetch-instruments]]}}))

(re-frame/reg-event-fx
 ::fetch-depth-data
 (fn-traced [_ [_ instrument-id]]
            {:dispatch [:clear-error]
             :http-xhrio {:method :get
                          :uri (gstring/format "https://www.okex.com/api/spot/v3/instruments/%s/book" instrument-id)
                          :timeout 8000
                          :response-format (ajax/json-response-format {:keywords? true})
                          :on-success [:set-depth-data]
                          :on-failure [:error [:fetch-depth-data]]}}))

;;; =================== fx event
(re-frame/reg-event-fx
 :timer
 (fn [{:keys [db]} _]
   (when-let [instrument-id (get-instrument-id db)]
     {:dispatch [::fetch-depth-data instrument-id]})))
