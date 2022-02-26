(ns aoc2021.day18
  (:require [clojure.test :refer [deftest are]]))

(def pair list)
(def left first)
(def right second)
(def add concat)
(defn split? [p] (and (number? p) (>= p 10)))

(defn split [x]
  (let [y (quot x 2)]
    (if (even? x) (pair y y)
      (pair y (inc y)))))

(defn pair-reduce [p])

(defn magnitude [p]
  (if (number? p) p
    (+ (* 3 (magnitude (left p)))
       (* 2 (magnitude (right p))))))

(defn number-pair? [p] (every? number? pair))
(defn explode [expr depth]
  (if (= depth 4)
    ))

(deftest split-test
  (are [in out] (= out (split in))
       10 [5 5]
       11 [5 6]
       12 [6 6]))

(deftest mag-test
  (are [in out] (= out (magnitude in))
       4 4
       [[1,2],[[3,4],5]] 143
       [[[[0,7],4],[[7,8],[6,0]]],[8,1]] 1384
       [[[[1,1],[2,2]],[3,3]],[4,4]] 445
       [[[[3,0],[5,3]],[4,4]],[5,5]] 791
       [[[[5,0],[7,4]],[5,5]],[6,6]] 1137
       [[[[8,7],[7,7]],[[8,6],[7,7]]],[[[0,7],[6,6]],[8,7]]] 3488))
