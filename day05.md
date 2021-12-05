# Day 5: Hydrothermal Venture

A coordinate system. The input (e.g. `0,9 -> 5,9`) describes 'lines' of coordinates.

Considering horizontal lines only, at how many points do at least two lines overlap?

A simple function here would be to take a line as an input and return a sequence of points. Then answering this question would be be a concatenation of all applying that function to all lines, then finding the frequency of the points.

We'll also need a way to detect whether a line is horizontal or not.

No real tricks here. Only thing to note is that `range` expects its first argument to be less than its second, and the input doesn't conform to that.

``` clojure
(defn horizontal? [line]
  (let [[x1 y1 x2 y2] (extract-ints line)]
    (or (= x1 x2) (= y1 y2))))

(defn points-in-line [line]
  (let [[x1 y1 x2 y2] (extract-ints line)]
    (for [x (range (min x1 x2) (inc (max x1 x2)))
          y (range (min y1 y2) (inc (max y1 y2)))]
      [x y])))

(points-in-line "0,9 -> 5,9")
;; => ([0 9] [1 9] [2 9] [3 9] [4 9] [5 9])
(horizontal? "0,9 -> 5,9")
;; => true
(horizontal? "445,187 -> 912,654")
;; => false

(->> (filter horizontal? input)
     (mapcat points-in-line)
     (frequencies)
     (filter #(> (second %) 1))
     (count))
;; => 5197
```

For part 2 we need to extend it to diagonal lines. I think we can do this just by changing the 'points in line' function (which won't work in current form). There is the constraint that all diagonal lines are at 45 degrees, so x+1 and y+1 for every move. You just need to figure out the direction.

``` clojure
(defn dir [a b]
  (cond (< a b) inc
        (> a b) dec
        :else identity))

(defn points-in-line [line]
  (let [[x1 y1 x2 y2] (extract-ints line)
        nxt (fn [[x y]] [((dir x1 x2) x) ((dir y1 y2) y)])]
    (conj (->> (iterate nxt [x1 y1])
               (take-while #(not= % [x2 y2])))
          [x2 y2])))
```

This has the nice property that not only does out solving code for part 1 not need to change, but our solution code for part 2 just _removes_ a transform from part 1.

./clojure/src/aoc2021/day05.clj[link to src]

``` clojure
(defn extract-ints [coll] (mapv #(Long/parseLong %) (re-seq #"\d+" coll)))

(defn horizontal? [line]
  (let [[x1 y1 x2 y2] (extract-ints line)]
    (or (= x1 x2) (= y1 y2))))

(defn dir [a b]
  (cond (< a b) inc
        (> a b) dec
        :else identity))

(defn points-in-line [line]
  (let [[x1 y1 x2 y2] (extract-ints line)
        nxt (fn [[x y]] [((dir x1 x2) x) ((dir y1 y2) y)])]
    (conj (->> (iterate nxt [x1 y1])
               (take-while #(not= % [x2 y2])))
          [x2 y2])))

(comment
  (->> input
       (filter horizontal?)
       (mapcat points-in-line)
       (frequencies)
       (filter #(> (second %) 1))
       (count))
;; => 5197
  (->> input
       (mapcat points-in-line)
       (frequencies)
       (filter #(> (second %) 1))
       (count))
  ;; => 18605

  )
```

