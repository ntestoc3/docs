(ns okex-web.utils
  (:require [clojure.spec.alpha :as s]
            [re-frame.core :as re-frame]
            [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]))

;;;;;;;;;;;;;;;;; helper function

(defn negligible?
  "Variant of empty? that behaves reasonably for non-seqs too.
  Note that nil is negligible but false is not negligible."
  [x]
  (cond (seqable? x) (empty? x)
        (boolean? x) false
        :else (not x)))

(defn validate
  "Like s/valid?, but show the error like s/assert. Useful for pre-conditions."
  [spec x]
  (or (s/valid? spec x)
      (s/explain spec x)))

;;;;;;;;;;;;;;;;;;

(defn sub2
  "Shorthand for simple 'layer 2` usage of re-sub
  `key` sub key
  `db-path` vector of db keys"
  [key db-path]
  (re-frame/reg-sub
   key
   (fn-traced [db _] (get-in db db-path))))

(defn evt-db2
  "Shorthand for simple db event, like sub2
  `key` event key
  `db-path` vector of db keys
  `value` 直接返回指定值"
  ([key db-path]
   (re-frame/reg-event-db
    key
    (fn-traced [db [_ value]]
      (assoc-in db db-path value))))
  ([key db-path value]
   (re-frame/reg-event-db
    key
    (fn-traced [db _]
      (assoc-in db db-path value)))))

(def <sub (comp deref re-frame/subscribe))

;; Ideas based on https://lambdaisland.com/blog/11-02-2017-re-frame-form-1-subscriptions

(defn >evt
  "Shorthand for re-frame dispatch to event.
  The two-argument form appends a value into the event.
  The three-argument form offers more control over this value, letting
  you specify a default value for it and/or a coercer (casting) function"
  ([event]
   (re-frame/dispatch event))
  ([event value]
   (re-frame/dispatch (conj event value)))
  ([event value {:keys [default coercer] :or {coercer identity}}]
   (>evt event
         (coercer (if (negligible? value)
                    default
                    value)))))

(s/def :re-frame/vec-or-fn (s/or :event-or-sub vector? :function fn?))

(defn- vec->fn [vec-or-fn key-fn]
  {:pre [(validate (s/nilable :re-frame/vec-or-fn) vec-or-fn)]
   :post (fn? %)}
  (if (vector? vec-or-fn)
    #(key-fn (conj vec-or-fn %))
    vec-or-fn))

(defn event->fn
  "For contexts that want to pass an argument to a sink function: accept
  either a function or a re-frame event vector.
  If a vector is received, convert it to a function that dispatches to that
  event, with the parameter conj'd on to the end."
  [event-or-fn]
  (vec->fn event-or-fn >evt))

(defn sub->fn
  "Accept either a re-frame sub or a function, for contexts that demand
  a function."
  [sub-or-fn]
  (vec->fn sub-or-fn <sub))
