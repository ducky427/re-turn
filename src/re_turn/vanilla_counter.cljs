(ns re-turn.vanilla-counter
  (:require [re-turn.helper :as r]))

(defn- by-id [id]
  (.getElementById js/document (name id)))

(defonce store (r/create-store 0))

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

(def value-el (by-id :value))

(defn- render
  []
  (set! (.-innerHTML value-el) (str (r/get-state store))))

(defn ^:export init!
  []
  (r/subscribe store render)
  (.addEventListener (by-id :increment) "click" #(r/dispatch store :counter/increment))
  (.addEventListener (by-id :decrement) "click" #(r/dispatch store :counter/decrement))
  (.addEventListener (by-id :incrementIfOdd) "click" #(when-not (zero? (mod (r/get-state store) 2))
                                                        (r/dispatch store :counter/increment)))
  (.addEventListener (by-id :incrementAsync) "click" #(js/setTimeout (fn []
                                                                       (r/dispatch store :counter/increment))
                                                                     1000)))
