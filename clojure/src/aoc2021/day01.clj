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
