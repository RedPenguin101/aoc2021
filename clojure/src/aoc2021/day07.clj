(ns aoc2021.day07
  (:require [aoc2021.utils :as u]
            [aoc2021.chart :as c]))

(def input (u/extract-ints (slurp "resources/day07input.txt")))

(defn sum-distance [xs y]
  (apply + (map #(Math/abs (- y %)) xs)))

(comment
  (time (->> (range (apply min input) (apply max input))
             (map #(sum-distance input %))
             (apply min)))
;; Elapsed time: 5853.353022 msecs
;; => 328187
  )

(defn tri-num [x] (/ (* x (inc x)) 2))

(defn sum-distance2 [xs y]
  (apply + (map #(tri-num (Math/abs (- y %))) xs)))

(defn cost1 [xs]
  (fn [y]
    (long (apply + (map #(Math/abs (- y %)) xs)))))

(defn cost2 [xs]
  (fn [y]
    (long (apply + (map #(tri-num (Math/abs (- y %))) xs)))))


(comment
  (time (->> (range (apply min input) (apply max input))
             (map #(sum-distance2 input %))
             (apply min)))
;; Elapsed time: 5470.86931 msecs
;; => 91257582
  )

(comment
  (c/scatter (->> (range (apply min input) (apply max input))
                  (map (juxt identity #(sum-distance2 input %))))
             1000 1000)

  (c/scatter (->> (range (apply min input) (apply max input))
                  (map (juxt identity #(sum-distance input %))))
             1000 1000))