(ns aoc2021.day10
  (:require [clojure.string :as str]))

(def input (str/split-lines (slurp "resources/day10input.txt")))
(def brackets (apply hash-map "()<>{}[]"))
(defn matches? [a b] (= b (brackets a)))

(defn score-autocompletion [ac-string]
  (reduce (fn [a c] (+ (* a 5) ({\) 1 \] 2 \} 3 \> 4} c)))
          0 ac-string))

(defn median [coll] (nth (sort coll) (/ (count coll) 2)))

(defn parse ([input] (parse '() input))
  ([stack [nxt & rst]]
  (cond (not nxt) [:ac (map brackets stack)]

        ((set (keys brackets)) nxt)
        (recur (conj stack nxt) rst)

        (matches? (first stack) nxt)
        (recur (rest stack) rst)

        :else [:corruption nxt])))

(defn corrupt? [line]
  (let [[rslt out] (parse line)]
    (when (= rslt :corruption) ({\) 3 \] 57 \} 1197 \> 25137} out))))

(defn autocomplete? [line]
  (let [[rslt out] (parse line)]
    (when (= rslt :ac) (score-autocompletion out))))

(->> input (keep corrupt?) (apply +)) ; 387363
(->> input (keep autocomplete?) median) ; 4330777059

