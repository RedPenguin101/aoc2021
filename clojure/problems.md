# Advent of Code 2021

## Day 1: Sonar Sweep

Given a list of integers, count the number of times the item in the list increases. For example:

```
199 (N/A - no previous measurement)
200 (increased)
208 (increased)
210 (increased)
200 (decreased)
207 (increased)
240 (increased)
269 (increased)
260 (decreased)
263 (increased)
```

Has 7 increases.

Here I can create a sliding window of 2 over the sequence, and compare each element of the window, filtering if the first element is smaller than the second.

``` clojure
(count (filter #(apply < %) (partition 2 1 input)))
;; => 1715
```

Part 2 has us the number of times the sum of measurements in a 3 sliding window increases from the previous sum. We can use the same strategy with an addition step to sum.

``` clojure
(->> input
     (partition 3 1)
     (map #(apply + %))
     (partition 2 1)
     (filter #(< apply %))
     count)
;; => 1739
```