(ns aoc2021.day05
  (:require [clojure.string :as str]))

(def example (str/split-lines "0,9 -> 5,9\n8,0 -> 0,8\n9,4 -> 3,4\n2,2 -> 2,1\n7,0 -> 7,4\n6,4 -> 2,0\n0,9 -> 2,9\n3,4 -> 1,4\n0,0 -> 8,8\n5,5 -> 8,2"))
(def input (str/split-lines (slurp "resources/day05input.txt")))

(defn extract-ints [coll] (mapv #(Long/parseLong %) (re-seq #"\d+" coll)))

(defn horizontal? [line]
  (let [[x1 y1 x2 y2] (extract-ints line)]
    (or (= x1 x2) (= y1 y2))))

(defn dir [a b]
  (cond (< a b) inc
        (> a b) dec
        :else identity))

(defn points-in-line [line]
  (let [[x1 y1 x2 y2] (extract-ints line)
        nxt (fn [[x y]] [((dir x1 x2) x) ((dir y1 y2) y)])]
    (conj (->> (iterate nxt [x1 y1])
               (take-while #(not= % [x2 y2])))
          [x2 y2])))

(comment
  (->> input
       (filter horizontal?)
       (mapcat points-in-line)
       (frequencies)
       (filter #(> (second %) 1))
       (count))
;; => 5197
  (->> input
       (mapcat points-in-line)
       (frequencies)
       (filter #(> (second %) 1))
       (count))
  ;; => 18605
  )
