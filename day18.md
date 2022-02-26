# Day 18: Snailfish

A snailfish number (SN) is a cons cells of integers. For example:

```
[1,2]
[[1,2],3]
[9,[8,7]]
[[1,9],[8,5]]
```

To add two SN, concatenate them.

```
[1,2] + [[3,4],5]  [[1,2],[[3,4],5]]
```

To _reduce_ an SN, repeatedly apply the following rules until no further reduction is possible:

1. If a pair has a depth of 4, the leftmost pair explodes (?!?)
2. If a number is >= 10, the leftmost such regular number splits.

The matching of any one of these returns, so the process will restart.

In an explosion, the pair's left value is added to the first number to the left of the exploding pair, and the right value is added to first number to the right of the pair. The exploding pair is replaced with 0.

```
[[[[[9,8],1],2],3],4] -> [[[[0,9],2],3],4]
[7,[6,[5,[4,[3,2]]]]] -> [7,[6,[5,[7,0]]]]
[[6,[5,[4,[3,2]]]],1] -> [[6,[5,[7,0]]],3]
[[3,[2,[1,[7,3]]]],[6,[5,[4,[3,2]]]]] -> 
  [[3,[2,[8,0]]],[9,[5,[4,[3,2]]]]] 
[[3,[2,[8,0]]],[9,[5,[4,[3,2]]]]] -> [[3,[2,[8,0]]],[9,[5,[7,0]]]]
```

A Split replaces a number with a pair, with the remainder in case of an odd number being added to the right element of the pair: `10 -> [5,5], 11 -> [5,6]`

Your input is a list of SN. Add them together and find the _magnitude_ of the final result: A pair's magnitude `M(P)` is `3M(L) + 2M(R)`, where `L` and `R` are the left and right elements of the pair. If a Pair is just a number, `M(P)` is the number

## Solution

All of this is pretty trivial except explode and the actual reduction itself.

```clojure
(def pair  list)
(def left  first)
(def right second)
(def add   concat)

(defn split? [p] (and (number? p) (>= p 10)))

(defn split [x]
  (let [y (quot x 2)]
    (if (even? x) (pair y y)
      (pair y (inc y)))))

(defn magnitude [p]
  (if (number? p) p
    (+ (* 3 (magnitude (left p)))
       (* 2 (magnitude (right p))))))
```
