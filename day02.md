# Day 2: Dive!

You're given commands like `forward x`, `down y` `up z`. These change your position on two axes. Process all the commands and output the product of the x and y values.

I'm going to parse each instruction to a vector `[x y]`, which represents the delta in the x and y values, then just add them all together.

``` clojure
(defn parse [line]
  (let [x (Long/parseLong (re-find #"\d+" line))]
    (case (first line)
      \f [x 0]
      \u [0 (- x)]
      \d [0 x])))

(->> input
     (map parse)
     (apply map +)
     (apply *))
;; => 2073315
```

Part 2 modified it a bit: down increases a value 'aim', up decreases it. Forward increases your x position as before, but also increases your depth by `aim * param`.

So aim sounds like your slope, or angle. If your angle is 0, you won't move up or down. If your slope is 1, you will move down as much as you move across, etc.

What we need is a reduce function to process each new instruction.

The input is now `[p d-aim]` (delta-aim). In the reduce function, `proc`, we track the x and y positions, and the aim, and modify each of those variables accordingly.

``` clojure
(defn proc [[x y aim] [p d-aim]]
  [(+ x p)
   (+ y (* p aim))
   (+ aim d-aim)])

(->> input
     (map parse)
     (reduce proc [0 0 0])
     (take 2)
     (apply *))
;; => 1840311528
```
