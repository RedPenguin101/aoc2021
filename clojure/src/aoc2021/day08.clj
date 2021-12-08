(ns aoc2021.day08
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [aoc2021.utils :as u]))

(def input (str/split-lines (slurp "resources/day08input.txt")))

(def true-encodings
  {1 "cf"
   7 (set "acf")
   4 (set "bcdf")
   5 (set "abdfg")  ;; missing c and e
   2 (set "acdeg")  ;; missing b and f
   3 (set "acdfg")  ;; missing b and e
   9 (set "abcdfg") ;; missing e
   0 (set "abcefg") ;; missing d
   6 (set "abdefg") ;; missing c
   8 (set "abcdefg")})

{\a #{\d}
 \c #{\a \b} ; c missing in 6, 5
 \f #{\a \b} ; f missing in 2
 \e #{\c \g} ; e missing in 5, 3 and 9
 \g #{\c \g}
 \b #{\e \f}  ; b missing in 2 and 3
 \d #{\e \f}} ; d missing in 0

"decoder is like" {\a \b} "etc."

(def example "acedgfb cdfbe gcdfa fbcad dab cefabd cdfgeb eafb cagedb ab | cdfeb fcadb cdfeb cdbaf")

(defn parse-line [line]
  (map #(re-seq #"[a-z]+" %) (str/split line #"\|")))

(defn string-sort [string]
  (apply str (sort string)))

(->> input
     (map parse-line)
     (mapcat last)
     (filter #(#{2 4 3 7} (count %)))
     count)
;; => 493

(def start (zipmap "abcdefg" (repeat (set "abcdefg"))))
;; => {\a #{\a \b \c \d \e \f \g},
;;     \b #{\a \b \c \d \e \f \g},
;;     \c #{\a \b \c \d \e \f \g},
;;     \d #{\a \b \c \d \e \f \g},
;;     \e #{\a \b \c \d \e \f \g},
;;     \f #{\a \b \c \d \e \f \g},
;;     \g #{\a \b \c \d \e \f \g}}

(defn refine-uniques [decode-map]
  (let [founds (apply concat (filter #(= 1 (count %)) (vals decode-map)))]
    (u/map-vals #(if (= 1 (count %)) % (set/difference % founds)) decode-map)))

(defn refine-doubles [decode-map]
  (let [doubles (set (keys (filter (fn [[k v]] (and (= 2 (count k)) (= 2 v))) (frequencies (vals decode-map)))))]
    (u/map-vals #(if (doubles %) % (set/difference % (apply set/union doubles)))
                decode-map)))

(defn refine [decode-map]
  (let [nxt (-> decode-map refine-uniques refine-doubles)]
    (if (= decode-map nxt) nxt
        (recur nxt))))

(defn eliminate2 [decode-map kys vls]
  (reduce (fn [A k]
            (update A k set/intersection vls))
          decode-map
          kys))

(defn eliminate [decode-map value]
  (case (count value)
    2 (refine (eliminate2 decode-map (true-encodings 1) (set value)))
    3 (refine (eliminate2 decode-map (true-encodings 7) (set value)))
    4 (refine (eliminate2 decode-map (true-encodings 4) (set value)))
    decode-map))

(reduce eliminate start (first (parse-line example)))


(butlast (drop 3 (sort-by count (map string-sort (first (parse-line example))))))
'("bcdef" "acdfg" "abcdf" "abcdef" "bcdefg" "abcdeg")

(defn unique-letters [strings]
  (apply set/union
         (for [x (map set strings) y (map set strings)]
           (set/difference x y))))

(set/difference (set "abcdef") (set "bcdefg"))

(unique-letters '("abcdef" "bcdefg" "abcdeg"))

(defn find-f [strings])
(frequencies (mapcat #(set/difference (set "abcdefg") (set %)) '("bcdef" "acdfg" "abcdf" "abcdef" "bcdefg" "abcdeg")))

(frequencies (mapcat #(set/difference (set "abcdefg") %)
                     [(set "abdfg")
                      (set "acdeg")
                      (set "acdfg")
                      #_#_#_(set "abcdfg")
                          (set "abcefg")
                        (set "abdefg")]))