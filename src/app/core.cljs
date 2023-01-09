(ns app.core
  "This namespace contains your application and is the entrypoint for 'yarn start'."
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [reagent.ratom :as ratom :refer [reaction]]))

;; methods
(defn handle-search-input-entered
  [app-state [_ search-input]]
  (assoc-in app-state [:search-input] search-input))

(defn matches-query?
  [search-input phone]
  (if (= "" search-input)
    true
    (boolean (or
              (re-find (re-pattern search-input) (:name phone))
              (re-find (re-pattern search-input) (:snippet phone))))))
(defn handle-order-prop-changed
  [app-state [_ order-prop]]
  (assoc-in app-state [:order-prop] order-prop))



(defn mark-selected
  [props order-prop current-prop-value]
  (if (= order-prop current-prop-value)
    (r/merge-props props {:selected "selected"})
    props))


;; Handlers
(rf/register-handler
 :initialize-db
 (fn [_ _]
   {:phones [{:name "Nexus S" :snippet "Fast just got faster with Nexus S"}
             {:name "Motorola XOOM™ with Wi-Fi" :snippet "The Next, Next Generation tablet."}
             {:name "Motoral Xoom" :snippet "The Next, Next Generation tablet."}]
    :search-input ""
    :order-prop "name"}))

(rf/register-handler
 :search-input-entered
 handle-search-input-entered)

(rf/register-handler
 :order-prop-changed
 handle-order-prop-changed)

;; Subscriptions
(rf/register-sub
 :phones
 (fn [db]
   (reaction (:phones @db))))

(rf/register-sub
 :search-input
 (fn [db]
   (reaction (:search-input @db))))

(rf/register-sub
 :order-prop
 (fn
  [db]
  (reaction (:order-prop @db))))



;; Views

(defn phone-component
  [phone]
  [:li
   [:span (:name phone)]
   [:p (:snippet phone)]])

(defn phones-component
  []
  (let [phones (rf/subscribe [:phones])
        search-input (rf/subscribe [:search-input])
        order-prop (rf/subscribe [:order-prop])]
    (fn []
      [:ul (for [phone (->> @phones
                            (filter (partial matches-query? @search-input))
                            (sort-by (keyword @order-prop)))]
             ^{:key phone} [phone-component phone])])))

(defn search-component
  []
  (let [search-input (rf/subscribe [:search-input])])
  (fn []
    [:input {:on-change #(rf/dispatch [:search-input-entered (-> % .-target .-value)])}]))

(defn order-by-component
  []
  (let [order-prop (rf/subscribe [:order-prop])]
    (fn []
      [:div "Sort by: "
       [:select {:on-change #(rf/dispatch [:order-prop-changed (-> % .-target .-value)])}
        [:option (mark-selected {:value "name"} @order-prop "name") "Alphabetical"]
        [:option (mark-selected {:value "age"} @order-prop "age") "Newest"]]])))

;; home page hiccup
(defn home-page []
  [:div
   [:div
    [search-component]]
   [:div
    [order-by-component]]
   [:div
    [phones-component]]])



(defn ^:dev/after-load render
  "Render the toplevel component for this app."
  []
  (rf/dispatch [:initialize-db])
  (r/render [home-page] (.getElementById js/document "app")))

(defn ^:export main
  "Run application startup logic."
  []
  (render))
