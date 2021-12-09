(ns aoc2021.day07
  (:require [aoc2021.utils :as u]
            [aoc2021.chart :as c]))

(def input (u/extract-ints (slurp "resources/day07input.txt")))
(defn tri-num [x] (/ (* x (inc x)) 2))
(defn sum-distance [cost-fn xs y] (apply + (map #(cost-fn (Math/abs (- y %))) xs)))
(defn mean   [input] (float (/ (apply + input) (count input))))
(defn median [input] (nth (sort input) (/ (count input) 2)))

;; part 1
(sum-distance identity input (median input))
;; => 328187

;; part 2
(min (sum-distance tri-num input (Math/round (- (mean input) 0.5)))
     (sum-distance tri-num input (Math/round (+ (mean input) 0.5))))
;; => 91257582
