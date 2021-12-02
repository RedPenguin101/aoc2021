(ns aoc2021.day02
  (:require [clojure.string :as str]))

(def input (str/split-lines (slurp "resources/day02input.txt")))

(defn parse [line]
  (let [x (Long/parseLong (re-find #"\d+" line))]
    (case (first line)
      \f [x 0]
      \u [0 (- x)]
      \d [0 x])))

(->> input
     (map parse)
     (apply map +)
     (apply *))
;; => 2073315

(defn proc [[x y aim] [p d-aim]]
  [(+ x p)
   (+ y (* p aim))
   (+ aim d-aim)])

(->> (str/split-lines "forward 5\ndown 5\nforward 8\nup 3\ndown 8\nforward 2")
     (map parse)
     (reduce proc [0 0 0])
     (take 2)
     (apply *))

(->> input
     (map parse)
     (reduce proc [0 0 0])
     (take 2)
     (apply *))
;; => 1840311528
