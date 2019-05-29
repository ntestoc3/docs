(ns core
  (:require [seesaw.core :as gui]
            [seesaw.table :as table]
            [seesaw.bind :as bind]
            [seesaw.table :refer [table-model]]
            [api]
            [taoensso.timbre :as log])
  (:use com.rpl.specter))

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

;;; 设置form的默认值
(let [first-base (first base-coins)]
  (def coin-pair-data (atom {:base-coin first-base
                             :quote-coin (-> (get-quote-coins first-base)
                                             first)})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn depth-data-model
  "深度数据table模型"
  [data]
  (table-model :columns [{:key :pos :text "价位"}
                         {:key :price :text "价格"}
                         {:key :amount :text "数量"}
                         {:key :order-count :text "订单数"}]
               :rows data))

(defn make-depth-view
  []
  (let [bids-view (gui/vertical-panel
                   :items [(gui/label "买入信息")
                           (gui/scrollable
                            (gui/table
                             :id :bids-table
                             :model (depth-data-model [])))])

        asks-view (gui/vertical-panel
                   :items [(gui/label "卖出信息")
                           (gui/scrollable
                            (gui/table
                             :id :asks-table
                             :model (depth-data-model [])))])

        coin-pair-selector (gui/horizontal-panel
                            :items [(gui/label "基准币种:")
                                    (gui/combobox :id :base-coin
                                                  :model base-coins)
                                    (gui/label "计价币种:")
                                    (gui/combobox :id :quote-coin)])]
    (gui/border-panel
     :north coin-pair-selector
     :center (gui/horizontal-panel
              :items [bids-view
                      asks-view])
     :vgap 5 :hgap 5 :border 3)))

(defn update-quote-coin-model!
  "更新计价货币的模型"
  [f model]
  (let [quote-coin (gui/select f [:#quote-coin])]
    (gui/config! quote-coin :model model)))

(defn depth-table-update!
  "更新depth数据显示"
  [root]
  (let [coin-p @coin-pair-data
        instrument-id (get-instrument-id (:base-coin coin-p)
                                         (:quote-coin coin-p))
        data (api/get-spot-instrument-book instrument-id)
        bids-table (gui/select root [:#bids-table])
        asks-table (gui/select root [:#asks-table])]
    (->> (:bids data)
         depth-data-model
         (gui/config! bids-table :model))
    (->> (:asks data)
         depth-data-model
         (gui/config! asks-table :model))))

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
      (bind/property quote-coin :model)
      (bind/b-swap! coin-pair-data assoc :base-coin)))

    ;; 计价货币选择事件绑定
    (bind/bind
     (bind/selection quote-coin)
     (bind/b-swap! coin-pair-data assoc :quote-coin))

    ;; 定时更新depth-view
    (gui/timer (fn [_]
                 (depth-table-update! root)) :delay 100)

    ;; coin-pair-data修改就更新depth-view
    (add-watch coin-pair-data :depth-view (fn [k _ _ new-data]
                                            (depth-table-update! root)))))

(defn -main [& args]
  (gui/invoke-later
   (let [frame (gui/frame :title "okex 行情信息"
                          :on-close :exit ;; 窗口关闭时退出程序
                          :content (make-depth-view))]
     (update-quote-coin-model! frame (-> (:base-coin @coin-pair-data)
                                         get-quote-coins))
     (gui/value! frame @coin-pair-data)
     (add-behaviors frame)
     (-> frame gui/pack! gui/show!))))

