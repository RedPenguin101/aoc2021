(ns aoc2021.day04
  (:require [clojure.string :as str]
            [clojure.set :as set]))

(defn extract-ints [coll] (mapv #(Long/parseLong %) (re-seq #"\d+" coll)))
(defn pivot [xs] (apply map vector xs))

(defn parse-boards [lines]
  (let [[board-lines remaining]  (split-with #(not= % "") lines)]
    (when (not-empty board-lines)
      (cons (mapv extract-ints board-lines) (parse-boards (rest remaining))))))

(def input (str/split-lines (slurp "resources/day04input.txt")))
(def draws (extract-ints (first input)))
(def boards (parse-boards (drop 2 input)))

(defn has-won? [board draws]
  (some #(set/subset? (set %) (set draws)) (into board (pivot board))))

(defn time-to-win [board cumulative-draws]
  (count (take-while #(not (has-won? board %)) cumulative-draws)))

(defn sort-by-time-to-win [boards cumulative-draws]
  (->> boards
       (map (juxt #(time-to-win % cumulative-draws) identity))
       (sort-by first)
       (map second)))

(defn score-winning-state [board cumulative-draws]
  (let [win-draws (first (drop-while #(not (has-won? board %)) cumulative-draws))]
    (->> (set win-draws)
         (set/difference (set (flatten board)))
         (apply +)
         (* (last win-draws)))))

(comment
  (time (let [cumulative-draws (reductions conj [] draws)
              sorted-boards (sort-by-time-to-win boards cumulative-draws)]
          [:quickest
           (score-winning-state (first sorted-boards) cumulative-draws)
           :slowest
           (score-winning-state (last sorted-boards) cumulative-draws)]))
  ;; Elapsed time: 367.260945 msecs
  ;; => [:quickest 31424 :slowest 23042]
  )
