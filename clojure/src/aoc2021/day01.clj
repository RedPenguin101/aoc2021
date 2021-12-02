(ns aoc2021.day01
  (:require [clojure.string :as str]))

(def input (map #(Long/parseLong %) (str/split-lines (slurp "resources/day01input.txt"))))

(count (filter #(apply < %) (partition 2 1 input)))
;; => 1715

(->> input
     (partition 3 1)
     (map #(apply + %))
     (partition 2 1)
     (filter #(apply < %))
     count)
;; => 1739

(comment
  "beautiful solution from
   https://github.com/tschady/advent-of-code/blob/main/src/aoc/2021/d01.clj"

  (count (filter pos? (map - (rest input) input)))
  ;; => 1715

  (count (filter pos? (map - (drop 3 input) input)))
  ;; => 1739
  )