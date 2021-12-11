(ns aoc2021.day09
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [aoc2021.utils :as u]))

(def input (u/coordinate (mapv u/digits (str/split-lines (slurp "resources/day09input.txt")))))

(defn adjacents [[x y]] (set [[(inc x) y] [(dec x) y] [x (inc y)] [x (dec y)]]))

(defn low-point? [coord coord-map]
  (< (coord-map coord)
     (apply min (vals (select-keys coord-map (adjacents coord))))))

(defn low-points [coord-map] (filter #(low-point? % input) (keys coord-map)))

(->> (low-points input)
     (map input)
     (map inc)
     (apply +))
;; => 522

(defn find-basin
  ([coord-map start] (find-basin coord-map #{start} #{}))
  ([coord-map basin tried]
   (if-let [nxt (first (set/difference basin tried))]
     (recur coord-map
            (into basin (->> (set/difference (adjacents nxt) basin)
                             (remove #(= 9 (get coord-map % 9)))))
            (conj tried nxt))
     basin)))

(time (->> (low-points input)
           (map #(find-basin input %))
           (map count)
           (sort >)
           (take 3)
           (apply *)))
;; Elapsed time: 120.022713 msecs
;; => 916688
