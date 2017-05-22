(ns re-turn.helper
  (:require [goog.object :as gobj]
            [cljsjs.redux]))

(defn logger
  [store]
  (fn [next]
    (fn [action]
      (js/console.log "Dispatching " action)
      (let [result (next action)]
        (js/console.log "next state " (.getState store))
        result))))

(defprotocol Store
  (add-reducer [this type f])
  (dispatch [this type] [this type data])
  (subscribe [this f])
  (get-state [this]))

(defrecord ReduxStore [store !handlers]
  Store
  (add-reducer [this type f]
    (swap! !handlers assoc type f))
  (dispatch [this type]
    (dispatch this type nil))
  (dispatch [this type data]
    (let [fs  @!handlers
          f   (if (contains? fs type)
                (type fs)
                (do
                  (js/console.warn "Unable to find handler for action: " type)
                  identity))]
      (.dispatch store #js {"type" type "data" data "handler" f})))
  (subscribe [this f]
    (.subscribe store f))
  (get-state [this]
    (.getState store)))

(defn- new-reducer
  [store y]
  (let [type   (gobj/get y "type")
        f      (or (gobj/get y "handler")
                   identity)
        res    (f store type (gobj/get y "data"))]
    (if res
      res
      (do
        (js/console.warn "Got nil result for action: " y)
        store))))

(defn create-store
  ([initial]
   (ReduxStore. (js/Redux.createStore new-reducer
                                     initial)
               (atom {})))
  ([initial middlewares]
   (ReduxStore. (js/Redux.createStore new-reducer
                                     initial
                                     (apply js/Redux.applyMiddleware middlewares))
               (atom {}))))

(defn defreducer
  [store type f]
  (add-reducer store type f)
  f)
