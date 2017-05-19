(ns re-turn.helper
  (:require [goog.object :as gobj]
            [cljsjs.redux]))

(def ^:private !handlers (atom {}))

(defn logger
  [store]
  (fn [next]
    (fn [action]
      (js/console.log "Dispatching " action)
      (let [result (next action)]
        (js/console.log "next state " (.getState store))
        result))))

(defprotocol Store
  (dispatch [this type] [this type data])
  (subscribe [this f])
  (get-state [this]))

(defrecord ReduxStore [store]
  Store
  (dispatch [this type]
    (.dispatch store #js {"type" type}))
  (dispatch [this type data]
    (.dispatch store #js {"type" type "data" data}))
  (subscribe [this f]
    (.subscribe store f))
  (get-state [this]
    (.getState store)))

(defn create-store
  [initial]
  (let [new-reducer (fn [store y]
                      (let [type      (gobj/get y "type")
                            handlers  @!handlers]
                        (if (contains? handlers type)
                          (let [f       (type handlers)
                                res     (f store type (gobj/get y "data"))]
                            (if res
                              res
                              (do
                                (js/console.warn "Got nil result for action: " y)
                                store)))
                          (do
                            (js/console.warn "Unable to find handler for action: " y)
                            store))))]
    (ReduxStore. (js/Redux.createStore new-reducer
                                       initial
                                       (js/Redux.applyMiddleware logger)))))

(defn defreducer
  [type f]
  (swap! !handlers assoc type f)
  f)
