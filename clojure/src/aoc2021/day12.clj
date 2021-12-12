(ns aoc2021.day12
  (:require [clojure.string :as str]
            [clojure.set :as set]))

(def graph (mapcat #((juxt identity reverse) (str/split % #"-")) (str/split-lines (slurp "resources/day12input.txt"))))

(defn children [graph node] (set (keep #(when (= node (first %)) (second %)) graph)))
(defn lower-case? [string] (= (str/lower-case string) string))

(defn with-no-lc-revisits [graph node path]
  (set/difference (children graph node) (set (filter lower-case? path))))

(defn with-one-lc-revisit [graph node path]
  (if ((set (vals (frequencies (filter lower-case? path)))) 2)
    (with-no-lc-revisits graph node path)
    (disj (children graph node) "start")))

(defn all-paths
  ([next-fn graph] (all-paths next-fn '("start") "end" graph))
  ([next-fn path to graph]
   (if (= (first path) to) (list path)
       (mapcat #(all-paths next-fn (conj path %) to graph) (next-fn graph (first path) path)))))

(comment
  (time (count (all-paths with-no-lc-revisits graph)))
  ;; Elapsed time: 210.555148 msecs
  ;; => 4241

  (time (count (all-paths with-one-lc-revisit graph)))
  ;; => Elapsed time: 8972.176427 msecs
  ;; => 122134
  )
