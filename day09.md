# Day 9: Smoke Basin

The input is a _height map_. A _low point_ is a point that is lower than any of it's adjacent locations. The _risk level_ is 1 plus its height.

## Part 1

**What is the sum of the risk levels of all low points on your heightmap?**

This seems like a case of turning the input into a coordinate system of integers, writing an adjacent function, and then mapping over it.

```clojure
(defn adjacents [[x y]] (set [[(inc x) y] [(dec x) y] [x (inc y)] [x (dec y)]]))

(defn low-point? [coord coord-map]
  (< (coord-map coord)
     (apply min (vals (select-keys coord-map (adjacents coord))))))

(defn low-points [coord-map] (filter #(low-point? % input) (keys coord-map)))

(->> (low-points input)
     (map input)
     (map inc)
     (apply +))
;; => 522
```

## Part 2

A _basin_ is all locations that flow down to a single low point. 9's don't count. The size of a basin is the number of coordinates in it.

**What do you get if you multiply together the sizes of the three largest basins?**

This is like a search, where starting from each low point, you find the adjacent values. If they are 9, you stop, otherwise you continue.

``` clojure
(defn find-basin
  ([coord-map start] (find-basin coord-map #{start} #{}))
  ([coord-map basin tried]
   (if-let [nxt (first (set/difference basin tried))]
     (recur coord-map
            (into basin (->> (set/difference (adjacents nxt) basin)
                             (remove #(= 9 (get coord-map % 9)))))
            (conj tried nxt))
     basin)))

(time (->> (low-points input)
           (map #(find-basin input %))
           (map count)
           (sort >)
           (take 3)
           (apply *)))
;; Elapsed time: 120.022713 msecs
;; => 916688
```

