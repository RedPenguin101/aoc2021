# Day 6: Lanternfish

The input is a sequence of integers, with each integer acting as a countdown to spawning a new integer. After passing 0, the counter resets to 6. A new instance is created with a counter of 8.

**How many lanternfish would there be after 80 days?**

We could brute force this, I think it would be easy enough, but that will almost certainly fall down in part 2. So let's see what we can do with some math.

So what we're looking for here is a formula that will determine how many fish will exist after t seconds. f(t)=x. c_x is the count of the fish.

t_0 is 1 fish with a timer of 8 (then for each input we just add the initial value).

If `t<8` then `c_x=1`. When `t=9`, `x=6,8 (c_x=2)`. It's that 6 vs. 8 thing that's going to make this tough I think.

I wrote a quick program to play out the sequence and explore it a bit.

``` clojure
(defn floored-dec [n]
  (if (> n 0) (dec n) 6))

(defn step [xs] 
  (into (map floored-dec xs) (repeat (count (filter zero? xs)) 8)))
```

I think the fact that the numbers are completely independent is important. For part 1 you can just calculate how many fish will result from 1 fish, multiply that by the count of the input, and add the sum of the input.

OK, actually you totally can't do that, because the initial input has a large effect

```
Initial   : fish after 
value     : 80 days
     0    :  1421
     1    :  1401
     2    :  1191
     3    :  1154
     4    :  1034
     5    :   950
     6    :   905
     7    :   779
     8    :   768
```

But given this table we can easily calculate the answer to part 1: `[3 4 3 1 2]` becomes `[1154 + 1034 + 1154 + 1401 + 1191]=5934`, which matches the output. Let's go with that, but 5 bucks says that part 2 is going to ask how many fish there are at day 1 billion or something.

``` clojure

(def after-80
  (into {} (for [x (range 0 9)]
             [x (count (last (take 81 (iterate step [x]))))])))

(->> (slurp "resources/day06input.txt")
     (re-seq #"\d+")
     (map (comp after-80 #(Long/parseLong %)))
     (apply +))
;; => 343441
```

## Part 2

And, as expected, part 2 is to calculate the number of fish at day 256. Not exactly 1 billion, but enough to make the brute force approach impossible.

So back to math.

My first idea is that, from a given starting point, if you know how many fish you end up with on day x (and the 'countdowns' remaining on each), you can apply that same calculation to each fish in the result to 'shortcut' the calculation to 2x. So if I can calculate how many fish with each countdown timer I end up with after t8, I can recursively apply that 32 times.

``` clojure

(def after-8
  (into {} (for [x (range 0 9)]
             [x (frequencies (last (take 9 (iterate step [x]))))])))
{0 {8 1, 1 1, 6 1},
 1 {2 1, 0 1},
 2 {3 1, 1 1},
 3 {4 1, 2 1},
 4 {5 1, 3 1}, ;<- starting with a 4 day counter
 5 {6 1, 4 1},
 6 {7 1, 5 1},
 7 {8 1, 6 1},
 8 {0 1}}
 ```

In this data structure, if a fish starts with a **4** day countdown, after 8 days there will be one fish with a 5 countdown, and one fish with a 3 countdown.

Then you can say, after 16 days, the fish-map will be

```
After 16 days:
1*f(5) + 1*f(4) = {6 1, 4 1} + {4 1, 2 1} = {2 1, 4 2, 6 1}

After 24 days:
1*f(2) + 2*f(4) + 1*(f6) etc.
```


``` clojure
(defn one-8-cycle [m]
  (apply merge-with +
         (for [[n2 c] m]
           (into {} (map (fn [[k v]] [k (* c v)]) (after-8 n2))))))

(->> (slurp "resources/day06input.txt")
     (re-seq #"\d+")
     (map #(Long/parseLong %))
     (frequencies)
     (iterate one-8-cycle)
     (take 33)
     (last)
     (vals)
     (apply +))
;; Elapsed time: 5.551119 msecs
;; => 1569108373832
```

## Refactor

So that solves the problem (and it is fast), but we can do better I think. We have to apply `one-8-cycle` 32 times (because 256/8 = 32), because it's effectively running through a cycle of 8 each time. In `one-8-cycle` it's taking that initial cycle of 8 as it's base. But once we have the cycle of 16, we should be able to use that to jump straight to a cycle of 32, etc.

Or put another way: we calculated the number of fish that would result from initial countdown values of 0 through 8, after 8 days. We can use that to look up the number of fish that would result after 16 days. And that to find 32 days etc. So what we will end up with after 5 `(log(256/8) / log2)` applications is a simple map that, for each starting value `0-8`, we can lookup how many fish there will be after 256 days. 

```
0 : 6703087164
7 : 3649885552
1 : 6206821033
4 : 4726100874
6 : 3989468462
3 : 5217223242
2 : 5617089148
5 : 4368232009
8 : 3369186778
```

``` clojure
(def after-8
  (into {} (for [x (range 0 9)] [x (frequencies (last (take 9 (iterate step [x]))))])))

(defn result-merge [n lookup]
  (->> (lookup n)
       (u/map-keys lookup)
       (map (fn [[k v]] (u/update-vals k * v)))
       (apply merge-with +)))

(defn double-lookup [lookup]
  (into {} (map (juxt identity #(result-merge % lookup)) (range 0 9))))

(def count-after-256
  (->> after-8
       (iterate double-lookup)
       (take 6)
       last
       (u/map-vals (comp #(apply + %) vals))))

(time (->> (u/extract-ints (slurp "resources/day06input.txt"))
           (map count-after-256)
           (apply +)))
;; Elapsed time: 0.673744 msecs
;; => Part 2: 1569108373832
```

A lot of work for 4ms, and I'm not sure the solution is much prettier. More work could be done on this for sure, I definitely feel there's an elegant recursive way to do this. But I can't wrap my head around it, and I've spent too long on it already.

