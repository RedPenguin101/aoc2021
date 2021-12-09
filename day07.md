# Day 7: Whales

Input is a list of integers, representing a 1d point.

What is the point that minimizes the sum of distances of each value from that point?

I don't really know how to think about this to be honest. Is it just the mean? Obviously too simple.

The formula for calculating the total distance from a point is: `d = Σ |x_i - y|`, which feels very familiar. It would be easy to brute force this I think. The numbers on the input span about 1000.

``` clojure
(defn sum-distance [xs y]
  (apply + (map #(Math/abs (- y %)) xs)))

(time (->> (range (apply min input) (apply max input))
           (map #(sum-distance input %))
           (apply min)))
;; Elapsed time: 5853.353022 msecs
;; => 328187
```

Part 2: the cost of moving one point now increases. The sequence is `Σ i`, or the triangle numbers, `f(x) = x(x+1)/2`

``` clojure
(defn tri-num [x] (/ (* x (inc x)) 2))

(defn sum-distance2 [xs y]
  (apply + (map #(tri-num (Math/abs (- y %))) xs)))

(time (->> (range (apply min input) (apply max input))
           (map #(sum-distance2 input %))
           (apply min)))
;; Elapsed time: 5470.86931 msecs
;; => 91257582
```

Obviously both parts are very slow, I'm sure there's efficiencies to be had. But my math isn't strong enough, I'm going to have to look it up.

I see two observations coming up on other peoples solutions:

1. The answer is close to the mean and/or median
2. You can avoid a linear search with a binary search

## An answer close to the median/mean

For part 1, the optimal convergence distance _k_ is the median. I don't know exactly how to prove this, but it is basically the definition of median.

``` clojure
(sum-distance input (median input))
;; => 328187
```

For part 2, the optimal convergence distance _k_ is within 0.5 of the mean. The math here is too much for me, but some super smart person wrote a [paper](https://www.reddit.com/gallery/rawxad) proving it.

``` clojure
(let [m (mean input)]
  (min (sum-distance2 input (Math/round (- m 0.5)))
       (sum-distance2 input (Math/round (+ m 0.5)))))
;; => 91257582
```

