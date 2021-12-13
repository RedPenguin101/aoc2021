(ns aoc2021.day13
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [aoc2021.utils :as u]))

(defn initial-canvas-size [folds]
  [(inc (* (second (first (drop-while #(not= 0 (first %)) folds))) 2))
   (inc (* (second (first (drop-while #(not= 1 (first %)) folds))) 2))])

(defn parse [input-str]
  (let [[coords instr] (str/split input-str #"\n\n")
        parsed-instr  (map vector (map #(if (= "x" %) 0 1) (re-seq #"[xy]" instr)) (u/extract-ints instr))]
    [{:dots (set (map vec (partition 2 (u/extract-ints coords))))
      :size (initial-canvas-size parsed-instr)}
     parsed-instr]))

(def input (parse (slurp "resources/day13input.txt")))

(defn new-coord [size axis fold-line coord]
  (when (> (get coord axis) fold-line)
    (update coord axis #(- (get size axis) % 1))))

(defn new-dots [dots size axis fold-line]
  (set/union (set (remove #(>= (get % axis) fold-line) dots))
             (set (keep #(new-coord size axis fold-line %) dots))))

(defn new-canvas-size [current axis] (update current axis #(/ (dec %) 2)))

(defn fold [canvas [axis fold-line]]
  (-> (update canvas :dots new-dots (:size canvas) axis fold-line)
      (update        :size new-canvas-size axis)))

(count (:dots (fold (first input) (first (second input)))))
; 737

(println (u/print-coord-set (:dots (apply reduce fold input))))
; "####.#..#...##.#..#..##..####.#..#.###..
;  ...#.#..#....#.#..#.#..#.#....#..#.#..#.
;  ..#..#..#....#.#..#.#..#.###..####.#..#.
;  .#...#..#....#.#..#.####.#....#..#.###..
;  #....#..#.#..#.#..#.#..#.#....#..#.#....
;  ####..##...##...##..#..#.#....#..#.#...."

