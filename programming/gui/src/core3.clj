(ns core3
  (:require [seesaw.core :as gui]
            [seesaw.table :as table]
            [seesaw.bind :as bind]
            [seesaw.table :refer [table-model]]
            [api]
            [taoensso.timbre :as log])
  (:use com.rpl.specter)
  (:import DepthWindow))


;;;;;;;;;;;;;;;;;;;;; Window-Builder binding

(defn identify
  "设置root下所有控件的seesaw :id"
  [root]
  (doseq [w (select root [:*])]
    (if-let [n (.getName w)]
      (selector/id-of! w (keyword n))))
  root)

;;;;;;;;;;;;;;;;;;;;;; 初始化值

(def coin-pairs "所有交易对信息" (api/get-instruments))
(def base-coins "所有基准货币"
  (-> (select [ALL :base-currency] coin-pairs)
      set
      sort))

(defn get-quote-coins
  "获取基准货币支持的计价货币"
  [base-coin]
  (select [ALL #(= (:base-currency %) base-coin) :quote-currency] coin-pairs))

(defn get-instrument-id
  "根据基准货币和计价货币获得币对名称"
  [base-coin quote-coin]
  (select-one [ALL
               #(and (= (:base-currency %) base-coin)
                     (= (:quote-currency %) quote-coin))
               :instrument-id]
              coin-pairs))

;; 设置form的默认值
(let [first-base (first base-coins)]
  (def coin-pair-data (atom {:base-coin first-base
                             :quote-coin (-> (get-quote-coins first-base)
                                             first)})))

;;;;;;;;;;;;;;;;;;;;;; 服务
(def instruments-info "交易对的深度数据"(atom {}))

(defn run-get-instrument-services!
  "启动获取交易对深度信息的服务
  没有提供停止功能"
  [instrument-id]
  (when (and instrument-id
             (not (contains? @instruments-info instrument-id)))
    (future (loop []
              (let [data (api/get-spot-instrument-book instrument-id)]
                (setval [ATOM instrument-id] data instruments-info))
              (Thread/sleep 200)
              (recur)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 辅助函数

(defn depth-data-model
  "深度数据table模型"
  [data]
  (table-model :columns [{:key :pos :text "价位"}
                         {:key :price :text "价格"}
                         {:key :amount :text "数量"}
                         {:key :order-count :text "订单数"}]
               :rows data))

(defn update-quote-coin-model!
  "更新计价货币的模型"
  [f model]
  (let [quote-coin (gui/select f [:#quote-coin])]
    (gui/config! quote-coin :model model)))

(defn get-current-instrument-id
  "获取当前币对的id"
  []
  (let [coin-p @coin-pair-data]
    (get-instrument-id (:base-coin coin-p)
                       (:quote-coin coin-p))))

(defn bind-transfrom-set-model
  [trans-fn frame id]
  (bind/bind
   (bind/transform #(trans-fn %))
   (bind/property (gui/select frame [id]) :model)))

(defn add-behaviors
  "添加事件处理"
  [root]
  (let [base-coin (gui/select root [:#base-coin])
        quote-coin (gui/select root [:#quote-coin])]
    ;; 基准货币选择事件绑定
    (bind/bind
     (bind/selection base-coin)
     (bind/transform get-quote-coins)
     (bind/tee
      ;; 设置quote-coin的选择项
      (bind/property quote-coin :model)
      (bind/bind
       (bind/transform first)
       (bind/selection quote-coin))))

    ;; 绑定基准货币和计价货币的选择事件
    (bind/bind
     (bind/funnel
      (bind/selection base-coin)
      (bind/selection quote-coin))
     (bind/transform (fn [[base-coin quote-coin]]
                       {:base-coin base-coin
                        :quote-coin quote-coin}))
     coin-pair-data)

    ;; 绑定交易对深度信息, 一旦更改就更新depth-view
    (bind/bind
     instruments-info
     (bind/transform #(% (get-current-instrument-id)))
     (bind/notify-later)
     (bind/tee
      (bind-transfrom-set-model #(-> (:bids %)
                                     depth-data-model) root :#bids-table)
      (bind-transfrom-set-model #(-> (:asks %)
                                     depth-data-model) root :#asks-table)))

    ;; 当前选择的交易对修改就启动新的深度信息服务
    (add-watch coin-pair-data :depth-view (fn [k _ _ new-data]
                                            (-> (get-current-instrument-id)
                                                run-get-instrument-services!)))))

(defn my-form
  []
  (let [from (identify (DepthWindow.))]))
(defn -main [& args]
  (gui/invoke-later
   (let [frame (gui/frame :title "okex 行情信息"
                          :on-close :exit ;; 窗口关闭时退出程序
                          :content (make-depth-view))]

     ;; 更新quote-coin的model
     (update-quote-coin-model! frame (-> (:base-coin @coin-pair-data)
                                         get-quote-coins))
     ;; 先绑定事件，再设置默认值
     (add-behaviors frame)
     (gui/value! frame @coin-pair-data)

     ;; 显示frame
     (-> frame gui/pack! gui/show!))))



