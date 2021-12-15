# Day 15: Chiton

A shortest path graph problem. Given an input like

```
1163751742
1381373672
2136511328
3694931569
7463417111
1319128137
1359912421
3125421639
1293138521
2311944581
```

Find the shortest path from top left to bottom right. _entering_ the location incurs the cost.

Here's a simple Dijskstra shortest path algorithm:

```clojure
(def inf ##Inf)

(defn build-graph [coords->costs]
  (for [coord (keys coords->costs)
        adj-coord (u/adjacent4 coord)
        :when (coords->costs adj-coord)]
    [coord adj-coord (coords->costs adj-coord)]))

(defn min-update-pq [pq [frm to cst]] 
  (update pq to (fnil min inf) (+ cst (pq frm))))

(defn connections [graph node exclude] 
  (filter #(and (= node (first %)) (not (exclude (second %)))) graph))

(defn quickest-route
  ([graph from to] (quickest-route graph to (priority-map from 0) #{}))
  ([graph target pq visited]
   (or (pq target)
       (recur graph target
              (dissoc (reduce min-update-pq pq (connections graph (ffirst pq) visited)) (ffirst pq))
              (conj visited (ffirst pq))))))
```

This is _very_ slow when run on the input (a 100x100 matrix): Nearly 2 minute run time.

There are no doubt many ways to improve this. One would be changing the algorithm to A*. Another would be fixing the awful `connections` algorithm. Because I am uber lazy, I'm just going to use Ubergraph, a graphing library.

```clojure
(time (let [graph (ug/add-directed-edges* (ug/digraph) (build-graph (u/coordinate input)))]
          (:cost (alg/shortest-path graph [0 0] [99 99] :weight))))
  ;; Elapsed time: 515.121312 msecs
  ;; => 390
```

Most of this is building the graph. The actual algorithm is sub 100ms.

## Part 2

Part 2 introduces a larger grid: the 100x100 grid we started with is repeated 5 times on both the x and y axis. Our 100x100 grid itself is now in a 5x5 grid. The values of the digits in each grid `[a b]` are `(inc (mod (+ a b (dec x)) 9))`. i.e. it wraps around to 1 if it passes 9.

There's probably a clever trick, but maybe I could just extend the original grid... Let's give that a go.

```clojure
(defn extend-grid [grid n]
  (letfn [(extend* [f n in] (apply concat (take n (iterate f in))))
          (wrap-inc [orig]  (inc (mod orig 9)))
          (map-wrap [n f]   (if (zero? n) f (recur (dec n) #(map f %))))]
    (->> grid
         (map #(extend* (map-wrap 1 wrap-inc) n %))
         (extend* (map-wrap 2 wrap-inc) n))))

(time (def graph2 (ug/add-directed-edges* (ug/digraph) (build-graph (u/coordinate (extend-grid input 5))))))
;; 13 seconds!

(time (:cost (alg/shortest-path graph2 [0 0] [499 499] :weight)))
;; 4.5 seconds
;; => 2814
```

Total ~ 20 seconds. Good enough.

