(ns okex-web.views
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com]
   [reagent.core :refer [atom]]
   [okex-web.utils :refer [>evt <sub]]
   [com.rpl.specter :as s]
   [okex-web.subs :as subs]
   ))

(defn depth-table
  [title data]
  (fn []
    [:div.container
     [:h4.text-center title]
     [:table.table.table-bordered
      [:thead
       [:tr
        [:th "价位"]
        [:th "价格"]
        [:th "数量"]
        [:th "订单数"]]]
      [:tbody
       (for [row data]
         ^{:key (str title (:pos row))}
         [:tr
          [:td (:pos row)]
          [:td (:price row)]
          [:td (:amount row)]
          [:td (:order-count row)]])]]]))

(defn vec->dropdown-choices
  ([v] (vec->dropdown-choices v nil))
  ([v group]
   (map #(hash-map :id % :label % :group group) v)))

(defn depth-view []
  (let [base-coins (re-frame/subscribe [::subs/base-coins])
        selected-base-coin (atom nil)
        quote-coins (atom [])
        selected-quote-coin (atom nil)
        bids (re-frame/subscribe [::subs/bids])
        asks (re-frame/subscribe [::subs/asks])]
    (fn []
      [re-com/v-box
       :gap "10px"
       :children [[re-com/h-box
                   :gap "10px"
                   :align :center
                   :children [[re-com/single-dropdown
                               :choices (vec->dropdown-choices @base-coins)
                               :model selected-base-coin
                               :placeholder "选择基准币种"
                               :filter-box? true
                               :on-change #(do
                                             (reset! selected-base-coin %)
                                             (reset! quote-coins (<sub [::subs/quote-coins %]))
                                             (reset! selected-quote-coin nil))]
                              [re-com/gap :size "10px"]
                              [re-com/single-dropdown
                               :choices (vec->dropdown-choices @quote-coins @selected-base-coin)
                               :model selected-quote-coin
                               :placeholder "选择计价币种"
                               :on-change #(reset! selected-quote-coin %)
                               ]
                              ]]
                  [re-com/h-split
                   :panel-1 [depth-table "买入信息" @bids]
                   :panel-2 [depth-table "卖出信息" @asks]]
                  ]])))


(defn title []
  (let [name (re-frame/subscribe [::subs/name])]
    [re-com/title
     :label @name
     :class "center-block"
     :level :level1]))

(defn main-panel []
  [:div.container
   [re-com/v-box
    :height "100%"
    :children [[title]
               [depth-view]
               ]]])
