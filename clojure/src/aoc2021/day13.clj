(ns aoc2021.day13
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [aoc2021.utils :as u]))

(defn parse [input-str]
  (let [[coords instr] (str/split input-str #"\n\n")]
    [(set (map vec (partition 2 (u/extract-ints coords))))
     (map vector (map #(if (= "x" %) 0 1) (re-seq #"[xy]" instr)) (u/extract-ints instr))]))

(def input (parse (slurp "resources/day13input.txt")))

(defn new-coord [axis fold-line coord]
  (when (> (get coord axis) fold-line)
    (update coord axis #(- (* 2 fold-line) %))))

(defn fold [dots [axis fold-line]]
  (set/union (set (remove #(>= (get % axis) fold-line) dots))
             (set (keep #(new-coord axis fold-line %) dots))))

(count (fold (first input) (first (second input))))
; 737

(println (u/print-coord-set (apply reduce fold input)))
; "░░░░.░..░...░░.░..░..░░..░░░░.░..░.░░░.
;  ...░.░..░....░.░..░.░..░.░....░..░.░..░
;  ..░..░..░....░.░..░.░..░.░░░..░░░░.░..░
;  .░...░..░....░.░..░.░░░░.░....░..░.░░░.
;  ░....░..░.░..░.░..░.░..░.░....░..░.░...
;  ░░░░..░░...░░...░░..░..░.░....░..░.░..."
