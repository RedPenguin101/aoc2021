(ns aoc2021.day11
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [aoc2021.utils :as u]))

(def input (u/coordinate (mapv u/digits (str/split-lines (slurp "resources/day11input.txt")))))

(defn new-energy [[k v] flash-points]
  (let [nv (+ v (count (set/intersection flash-points (u/adjacent8 k))))]
    [k (if (> nv 9) 0 nv)]))

(defn flash-chain
  ([grid] (flash-chain grid (set (map first (filter #(> (second %) 9) grid)))))
  ([grid flash-points]
   (let [new-grid (into {} (map #(new-energy % flash-points) grid))
         new-flash-points (set (keys (filter #(zero? (second %)) new-grid)))]
     (if (= flash-points new-flash-points) new-grid
         (recur grid new-flash-points)))))

(defn step [grid] (flash-chain (u/map-vals inc grid)))

(comment
  (->> (iterate step input)
       (take (inc 100))
       (rest)
       (mapcat #(filter zero? (vals %)))
       (count))
  ;; Part1 => 1702
  (->> (iterate step input)
       (take-while #(not (every? zero? (vals %))))
       (count))
  ;; Part2 => 251
  )

