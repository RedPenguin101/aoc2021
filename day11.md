# Day 11: Dumbo Octopus

The input is like yesterday: a fixed grid of digits.

Here, though, they are not static: each 'step' the digit increases by 1. When the digit reaches 9, it will roll over to 0, called a _flash_ in the context of the question. Any flash will increase the 8 adjacent values by one.

## Part 1: how many flashes after 100 steps

So this is a sort of finite game-of-lifey thing. The critical function is going to be a step, which calculates the next state.

One thing that isn't very clear to me from the description is the behavior when a point flashes due to having adjacent points flash. Say a point is at `8`, and 3 adjacent points flash. Does the point end up as `0` or `1`? The question has the following info:

> An octopus can only flash at most once per step ... any octopus that flashed during this step has its energy level set to 0, as it used all of its energy to flash.

This suggests that a point will only be set to `0`, even if the number of adjacent flashes would cause it to be 'above' 0.

The question breaks the step into 3 distinct phases: 1-increment, flash-chain, reset 0's. We can probably model it that same way.

* Step 1: iterate over the coordinate system, incrementing the value of each point. A value of > 9 will indicate a flash.
* Step 2: For each point, get the adjacents of the point. If they are 'flash', increment the value of the point. Bear in mind this can cause a 'flash-chain'.
* Step 3: reset all flashes to 0

Writing out the above, the thing that I think could cause problems is the chaining of flashes. If on the first pass of this, you cause another point to flash, you will have to run a second pass, and potentially additional passes. This means you have to keep track of the original value, and the increments separately, so you don't re-count.

OK, enough talk, time for code.

``` clojure
(defn new-vals [[k v] flash-points]
  [k (if (flash-points k) 0
         (+ v (count (set/intersection flash-points (u/adjacent8 k)))))])

(defn new-flash? [point grid current-flash]
  (when (> (+ (grid point) (count (set/intersection current-flash (u/adjacent8 point))))
           9) point))

(defn flash-chain
  ([grid] (flash-chain grid (set (map first (filter #(> (second %) 9) grid)))))
  ([grid flash-points]
   (let [new-flash (keep #(new-flash? % grid flash-points) (set/difference (set (keys grid)) flash-points))]
     (if (empty? new-flash) (into {} (map #(new-vals % flash-points) grid))
         (recur grid (set/union flash-points (set new-flash)))))))

(defn step [grid]
  (->> grid
       (u/map-vals inc)
       (flash-chain)))

(defn flash-count [steps init]
  (->> (iterate step init)
       (take (inc steps))
       (rest)
       (map #(count (filter zero? (vals %))))
       (apply +)))

(flash-count 100 input)
;; => 1702
```

## Part 2: what is the first iteration when every value is zero

Easy, we did all the work in part 1:

```clojure
  (->> (iterate step input)
       (take-while #(not (every? zero? (vals %))))
       (count))
  ;; Part2 => 251
```

## Refactor

First, we can get rid of `flash-count`, since it's never reused.

Second, the `new-vals` and `new-flash?` are doing very similar things. We can probably cut that down a bit

```clojure
(defn new-energy [[k v] flash-points]
  (let [nv (+ v (count (set/intersection flash-points (u/adjacent8 k))))]
    [k (if (> nv 9) 0 nv)]))

(defn flash-chain
  ([grid] (flash-chain grid (set (map first (filter #(> (second %) 9) grid)))))
  ([grid flash-points]
   (let [new-flash-points (keys (filter #(zero? (second (new-energy % flash-points))) (select-keys grid (set/difference (set (keys grid)) flash-points))))]
     (if (empty? new-flash-points) (into {} (map #(new-energy % flash-points) grid))
         (recur grid (set/union flash-points (set new-flash-points)))))))
```

Now we have a more generic `new-energy`, which, given a point->value pair and a list of flash-points, will return the new energy level of that point. This can be used both to see if a point is a flash-point, and to calculate the new energy of every point in the grid for the termination case.

The `new-flash-points` thing in flash-chain is awful though. And all it's really doing is testing the new-grid, which is effectively calculated a few lines later anyway (in the termination clause).

```clojure
(defn flash-chain
  ([grid] (flash-chain grid (set (map first (filter #(> (second %) 9) grid)))))
  ([grid flash-points]
   (let [new-grid (into {} (map #(new-energy % flash-points) grid))
         new-flash-points (set (keys (filter #(zero? (second %)) new-grid)))]
     (if (= flash-points new-flash-points) new-grid
         (recur grid new-flash-points)))))
```

Much better!

## Full code

[Link to source](./clojure/src/aoc2021/day11.clj)

```clojure
(def input (u/coordinate (mapv u/digits (str/split-lines (slurp "resources/day11input.txt")))))

(defn new-energy [[k v] flash-points]
  (let [nv (+ v (count (set/intersection flash-points (u/adjacent8 k))))]
    [k (if (> nv 9) 0 nv)]))

(defn flash-chain
  ([grid] (flash-chain grid (set (map first (filter #(> (second %) 9) grid)))))
  ([grid flash-points]
   (let [new-grid (into {} (map #(new-energy % flash-points) grid))
         new-flash-points (set (keys (filter #(zero? (second %)) new-grid)))]
     (if (= flash-points new-flash-points) new-grid
         (recur grid new-flash-points)))))

(defn step [grid] (flash-chain (u/map-vals inc grid)))

(comment
  (->> (iterate step input)
       (take (inc 100))
       (rest)
       (mapcat #(filter zero? (vals %)))
       (count))
  ;; Part1 => 1702
  (->> (iterate step input)
       (take-while #(not (every? zero? (vals %))))
       (count))
  ;; Part2 => 251
  )
```

