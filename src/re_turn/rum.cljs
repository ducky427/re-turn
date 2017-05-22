(ns re-turn.rum
  (:require [re-turn.helper :as r]
            [rum.core :as rum]))

(defonce store (r/create-store 5 [r/logger]))

(r/defreducer
  store
  :counter/increment
  (fn [db]
    (inc db)))

(r/defreducer
  store
  :counter/decrement
  (fn [db]
    (dec db)))

(rum/defc new-component
  [num]
  [:div
   [:h3 "Numbers"]
   [:ul (for [n (range num)]
          [:li {:key n} n])]
   [:button {:type "submit"
             :on-click #(r/dispatch store :counter/increment)}
    "Increment!"]
   [:button {:type "submit"
             :on-click #(r/dispatch store :counter/decrement)}
    "Decrement!"]])

(defn- render
  []
  (js/ReactDOM.render (new-component (r/get-state store))
                      (.getElementById js/document "app")))

(defn ^:export init!
  []
  (r/subscribe store render)
  (render))
