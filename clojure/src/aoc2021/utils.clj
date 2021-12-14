(ns aoc2021.utils
  (:require [clojure.string :as str]))

(defn extract-ints [coll] (mapv #(Long/parseLong %) (re-seq #"\d+" coll)))
(defn pivot [xs] (apply map vector xs))
(defn map-vals [f m] (into {} (map (fn [[k v]] [k (f v)]) m)))
(defn map-keys [f m] (into {} (map (fn [[k v]] [(f k) v]) m)))
(defn update-vals [m f & args] (into {} (map (fn [[k v]] [k (apply f v args)]) m)))
(defn digits [line] (mapv (comp #(Long/parseLong %) str) line))

(defn coordinate
  "Turns a 2d grid (coll of coll of x) into a map of coord->x"
  [grid]
  (into {} (apply concat (map-indexed (fn [y x-row] (map-indexed (fn [x v] [[x y] v]) x-row)) grid))))

(defn adjacent8 [[x y]]
  (set (for [x' (range -1 2) y' (range -1 2)
        :when (not= 0 x' y')]
          [(+ x x') (+ y y')])))

(defn print-coord-set [points]
  (let [[mx my] (map #(inc (apply max %)) (apply map vector points))]
    (str/join "\n" (for [y (range 0 my)]
      (apply str (for [x (range 0 mx)]
        (if (points [x y]) \░ \.)))))))

(defn dot-product [v1 v2] (apply + (map * v1 v2)))

(defn matrix-mult [v m]
  (map #(dot-product v %) (apply map vector m)))

(defn make-matrix [r c]
  (if (= r 1)
    (vec (repeat c 0))
    (vec (repeat r (vec (repeat c 0))))))

(defn first-and-last [coll] [(first coll) (last coll)])
