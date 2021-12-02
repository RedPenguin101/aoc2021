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

(->> input
     (map parse)
     (reduce proc [0 0 0])
     (take 2)
     (apply *))
;; => 1840311528

; Inelegant redo of part 1 using function from
; part 2, based on idea from tschady. Works better
; with his solution though.
(->> input
     (map parse)
     (reduce proc [0 0 0])
     ((juxt first last))
     (apply *))
;; => 2073315
