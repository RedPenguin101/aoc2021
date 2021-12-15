(ns aoc2021.day15
  (:require [clojure.string :as str]
            [aoc2021.utils :as u]
            [ubergraph.core :as ug]
            [ubergraph.alg :as alg]))

(def input (map u/digits (str/split-lines (slurp "resources/day15input.txt"))))

(defn build-graph [coords->costs]
  (for [coord (keys coords->costs)
        adj-coord (u/adjacent4 coord)
        :when (coords->costs adj-coord)]
    [coord adj-coord (coords->costs adj-coord)]))

(defn extend-grid [grid n]
  (letfn [(extend* [f n in] (apply concat (take n (iterate f in))))
          (wrap-inc [orig]  (inc (mod orig 9)))
          (map-wrap [n f]   (if (zero? n) f (recur (dec n) #(map f %))))]
    (->> grid
         (map #(extend* (map-wrap 1 wrap-inc) n %))
         (extend* (map-wrap 2 wrap-inc) n))))

(comment
  (def example (map u/digits (str/split-lines (slurp "resources/day15example.txt"))))
  (time (:cost (alg/shortest-path (ug/add-directed-edges* (ug/digraph) (build-graph (u/coordinate example))) [0 0] [9 9] :weight)))
  (time (:cost (alg/shortest-path (ug/add-directed-edges* (ug/digraph) (build-graph (u/coordinate (extend-grid example 5)))) [0 0] [49 49] :weight))))

(comment
  (time (:cost (alg/shortest-path (ug/add-directed-edges* (ug/digraph) (build-graph (u/coordinate input))) [0 0] [99 99] :weight))) ;; => 390 (0.5 seconds)
  (time (def graph2 (ug/add-directed-edges* (ug/digraph) (build-graph (u/coordinate (extend-grid input 5)))))) ;; 13 seconds!
  (time (:cost (alg/shortest-path graph2 [0 0] [499 499] :weight))) ;; => 2814 (4.5 seconds)
  )
