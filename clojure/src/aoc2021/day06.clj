(ns aoc2021.day06
  (:require [aoc2021.utils :as u]))

(defn floored-dec [n] (if (> n 0) (dec n) 6))

(defn step [xs]
  (into (map floored-dec xs) (repeat (count (filter zero? xs)) 8)))

(def after-80
  (into {} (for [x (range 0 9)]
             [x (count (last (take 81 (iterate step [x]))))])))

(->> (u/extract-ints (slurp "resources/day06input.txt"))
     (map after-80)
     (apply +))
;; => Part 1: 343441 

(def after-8
  (into {} (for [x (range 0 9)] [x (frequencies (last (take 9 (iterate step [x]))))])))

(defn result-merge [n lookup]
  (->> (lookup n)
       (u/map-keys lookup)
       (map (fn [[k v]] (u/update-vals k * v)))
       (apply merge-with +)))

(defn double-lookup [lookup]
  (into {} (map (juxt identity #(result-merge % lookup)) (range 0 9))))

(def count-after-256
  (->> after-8
       (iterate double-lookup)
       (take 6)
       last
       (u/map-vals (comp #(apply + %) vals))))

(time (->> (u/extract-ints (slurp "resources/day06input.txt"))
           (map count-after-256)
           (apply +)))
;; Elapsed time: 0.673744 msecs
;; => Part 2: 1569108373832
