(ns aoc2021.utils)

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
