(ns aoc2021.day14
  (:require [clojure.string :as str]
            [aoc2021.utils :as u]))

(defn parse-rule [line]
  (let [[in out] (re-seq #"[A-Z]+" line)]
    [[in (str (first in) out)] [in (str out (last in))]] ))

(let [[init-str rules] (str/split (slurp "resources/day14input.txt") #"\n\n")]
  (def input-str init-str)
  (def rules-parsed (mapcat parse-rule (str/split-lines rules))))

(defn matrix-from [in pos-map [r c]]
  (reduce (fn [m xs] (update-in m (map pos-map xs) inc))
          (u/make-matrix r c) in))

(defn repeated-mat-mult [n v m] (last (take (inc n) (iterate #(u/matrix-mult % m) v))))
(defn f [A [[a b] c]] (-> A (update a (fnil + 0) c) (update b (fnil + 0) c)))
(defn count-chars-from-pairs [pair-map] (u/update-vals (reduce f {} pair-map) / 2))
(defn most-least-diff [m] (apply - (u/first-and-last (sort > (map second m)))))

(defn calc [n input-str rules pos-map]
  (let [pairs (count pos-map)
        input-vector (matrix-from (map #(vector (apply str %)) (partition 2 1 input-str)) pos-map [1 pairs])
        rules-matrix (matrix-from rules pos-map [pairs pairs])]
    (->> (repeated-mat-mult n input-vector rules-matrix)
         (zipmap (sort (keys pos-map)))
         (merge-with + {(u/first-and-last input-str) 1})
         (count-chars-from-pairs)
         (most-least-diff))))

(def pos-map (zipmap (sort (set (apply concat rules-parsed))) (range)))

(calc 10 input-str rules-parsed pos-map)
; 2345

(time (calc 40 input-str rules-parsed pos-map))
; 132ms
; 2432786807053

