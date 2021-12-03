(ns aoc2021.day03
  (:require [clojure.string :as str]))

(def example (str/split-lines "00100\n11110\n10110\n10111\n10101\n01111\n00111\n11100\n10000\n11001\n00010\n01010"))
(def input (str/split-lines (slurp "resources/day03input.txt")))
(defn pivot [xs] (apply map vector xs))
(defn least-most-common [xs] (->> xs frequencies (sort-by val) (map first)))

(->> input
     pivot
     (map least-most-common)
     pivot
     (map #(Long/parseLong (apply str %) 2))
     (apply *))
;; => 852500

(defn common [most? xs]
  (let [freqs (frequencies xs)]
    (if (apply = (vals freqs)) (first (str most?))
        (nth (map first (sort-by val freqs)) most?))))

(defn bitwise-filter [bins idx most?]
  (if (= 1 (count bins)) (first bins)
      (let [pos #(nth % idx)
            test (common most? (map pos bins))]
        (recur (filter #(= test (pos %)) bins)
               (mod (inc idx) (count (first bins)))
               most?))))

(time (->> (map #(bitwise-filter input 0 %) [0 1])
           (map #(Long/parseLong % 2))
           (apply *)))
;; Elapsed time: 9.612306 msecs
;; => 1007985
