# Day 17: Trick Shot

A physics simulator: you launch a probe with initial velocities [vx vy], that follows a discrete trajectory determined by the following:

1. x increases by vx
2. y increases by vy
3. vx decreases by 1 (to zero)
4. vy decreases by 1

Given a target area range (x and y), some values of vx/vy result in the probe being in that range at some point, and some don't

The sample input is `target area: x=20..30, y=-10..-5`

My input is `target area: x=281..311, y=-74..-54`

## Part 1

**What is the highest you can make the probe go while still ending up in the target area?**

X and Y are independent, so I think these can be considered separately, at least a bit.

Since vx decreases by 1 each step, vx will have to be a minimum value to reach the target area at all. But I'm not sure we need to think about x at all in this problem: For any value of vy that passes through the range, there will be _some_ value of vx when it will pass through the range.

For y, the problem is that there is no terminal velocity. So at some point it will be moving downwards so fast it will miss the range completely. One thing I'm not clear on is that there _is_ an upper bound. Like, could there be some crazy huge value that, coincidently, has a y pos that intersects the range? The question certainly implies there is, so let's assume for now.

```clojure
(defn step [[x vx]] [(+ x vx) (dec vx)])

(defn try-y [init-vy y-max y-min]
  (let [traj (map first (take-while #(>= (first %) y-min) (iterate step [0 init-vy])))] 
       ;; traj is the list of all y values until the probe
       ;; is below the 'end' of the range.
    (when (<= (last traj) y-max) ;; when the last value is in the box
      (apply max traj)))) ;; get the max value of the trajectory

(try-y 9 -5 -10) ; 45

(last (take-while (comp not nil?) (map #(try-y % -5 -10) (range))))
; 45
```

So taking the last value before a `nil` result works for the sample. But not for the input!

```clojure
(take 5 (drop 25 (map #(try-y % -54 -74) (range)))) 
; (nil 351 378 406 435)
```

Note the `nil` followed by valid values. Which means we need a way to tell that there can be no possible future vys where the result ends in the box. i.e. you have to find that upper bound.

I think there's another way to thing about this. At the 'highest y value', your velocity is 0. And every step after this it decreases by 1. So you don't need to model the upward velocity at all. But this is where the question loses me. If you could work backwards (assign a very large negative vy to the probe while it's in the box), then surely the limit is infinite? vy+1 will give a higher maximum y value.

So why is `9` the greatest value for the example?

Let's do the backwards simulation: starting from some velocity fired directly upwards, what's the top-most point?

```clojure
(defn peak [y vy]
  (if (zero? vy) y
    (recur (+ y vy) (dec vy))))

(peak -10 10) ; 45
(peak -10 11) ; 56
```

So starting from a vy of (negative) 10, you get to the height of 45. But starting with 11, you get to 56. So is this value just not reachable given the starting conditions? We can simulate that with the same function:

```clojure
(peak 0 9) ; 45
(peak 0 10) ; 55
(peak 0 11) ; 66
```

Regardless of velocity, from starting point 0, you can never reach 56.

Try this: Think of a ball falling from the same position (falling and rising are the reverse of eachother, so it doesn't matter what direction we think about). To meet the criteria, it must hit both _exactly_ 0, and somewhere in the range -5 to -10. To do this, its velocity when it passes 0 must be below -10 (otherwise it will overshoot). So that's the cap.

Also, its vertical velocity at 0 is going to be exactly the same as its starting velocity but negative, I think. [actually, on experiment, one greater than]. So the vy is going to be `-ymax + 1`. For the example with the target ending at -10, that's 9, for my example, that's 73.

```clojure
(peak 0 (- 74 1)) ;=> 2701
```

The right answer! I should've got to it much quicker though.

## Part 2

**How many distinct initial velocity values cause the probe to be within the target area after any step?**

Again, I think we can use the independence of the directions here: find the number of x vals, and the number of y vals, then multiply them together.

Actually, maybe not. Looking at the example solution, there _aren't_ a square number of solutions, and `23,4` isn't a solution, despite `6,4` being a solution.

Looking at the solutions to the example, some patterns emerge (this is grouped by the vx val)

```
6  [(6 0) (6 1) (6 2) (6 3) (6 4) (6 5) (6 6) (6 7) (6 8) (6 9)]]
7  [(7 -1) (7 0) (7 1) (7 2) (7 3) (7 4) (7 5) (7 6) (7 7) (7 8) (7 9)]]
8  [(8 -2) (8 -1) (8 0) (8 1)]]
9  [(9 -2) (9 -1) (9 0)]]
10 [(10 -2) (10 -1)]]
11 [(11 -4) (11 -3) (11 -2) (11 -1)]]
12 [(12 -4) (12 -3) (12 -2)]]
13 [(13 -4) (13 -3) (13 -2)]]
14 [(14 -4) (14 -3) (14 -2)]]
15 [(15 -4) (15 -3) (15 -2)]]
20 [(20 -10) (20 -9) (20 -8) (20 -7) (20 -6) (20 -5)]]
21 [(21 -10) (21 -9) (21 -8) (21 -7) (21 -6) (21 -5)]]
22 [(22 -10) (22 -9) (22 -8) (22 -7) (22 -6) (22 -5)]]
23 [(23 -10) (23 -9) (23 -8) (23 -7) (23 -6) (23 -5)]]
24 [(24 -10) (24 -9) (24 -8) (24 -7) (24 -6) (24 -5)]]
25 [(25 -10) (25 -9) (25 -8) (25 -7) (25 -6) (25 -5)]]
26 [(26 -10) (26 -9) (26 -8) (26 -7) (26 -6) (26 -5)]]
27 [(27 -10) (27 -9) (27 -8) (27 -7) (27 -6) (27 -5)]]
28 [(28 -10) (28 -9) (28 -8) (28 -7) (28 -6) (28 -5)]]
29 [(29 -10) (29 -9) (29 -8) (29 -7) (29 -6) (29 -5)]]
30 [(30 -10) (30 -9) (30 -8) (30 -7) (30 -6) (30 -5)]])
```

First, the x vals fall into groups: 6-15, 20-30. For the sample range `20-30`, the correspondence of the second group is clear: a velocity of 20-30 will put the probe in the target range in 1 hop. Anything more will overshoot

It's let clear for the first group. Clearly there is a minimum velocity, below which the probe just won't reach the target (apparently here, 6) 

```clojure
(defn trajectory [x vx]
  (if (zero? vx) [x]
    (cons x (trajectory (+ x vx) (dec vx)))))

(trajectory 0 5) ; (0 5 9 12 14 15)
(trajectory 0 6) ; (0 6 11 15 18 20 21)
(take 5 (trajectory 0 15)) ; (0 15 29 42 54)
(take 5 (trajectory 0 16)) ; (0 16 31 45 58)
(take 5 (trajectory 0 19)) ; (0 19 37 54 70)
(take 5 (trajectory 0 20)) ; (0 20 39 57 74)
(take 5 (trajectory 0 31)) ; (0 31 61 90 118)
```

Then you have this 'missing' group in the middle: 16-19, where the probe 'skips' the target. This is evidently the non-inclusive range between half the upper bound (30) and the lower bound (20). This would assume that the difference between the lower an upper bound is small: if the upper bound were 31, then 16 would be viable.

In any case, we have an upper bound, and can probably just simulate everything up to that.

```clojure
(defn valid-vx? [vx x-min x-max]
  (<= x-min (last (take-while #(<= % x-max) (trajectory 0 vx)))))

(filter #(valid-vx? % 20 30) (range 0 (inc 30)))  
; (6 7 8 9 10 11 12 13 14 15 20 21 22 23 24 25 26 27 28 29 30)

(time (count (filter #(valid-vx? % 281 311) (range 0 (inc 311)))))
; 99, in about 2ms
```
Won't print the full output, but the start is 24, the end is 311, and the 'missing' ranges are:

* 48-49
* 55-59
* 65-71
* 80-94
* 105-140
* 157-280

There's probably a pattern there, but not going to dig into it.

What about the y-coords? The larger (20-30) x vals only have negative y vals. This makes sense, since all of these travel to the target in one step, so any vy must also be between ymax and ymin. So one set of [vx vy] is every value in the target ranges respectively.

Conversely, any vx which isn't in the x target range _can't_ have a vy that is in the target range, since it would take >1 steps to reach the box in the x direction, and on step 2 the y coord would have passed the box.

```clojure
(defn trajectory-y [y vy] 
  (map first (iterate (fn [[a b]] [(+ a b) (dec b)]) [y vy])))

(defn valid-vy? [vy y-min y-max]
  (>= y-max (last (take-while #(>= % y-min) (trajectory-y 0 vy)))))

(filter #(valid-vy? % -10 -5) (range -10 (inc 10))) 
; (-10 -9 -8 -7 -6 -5 -4 -3 -2 -1 0 1 2 3 4 5 6 7 8 9)

(time (count (filter #(valid-vy? % -74 -54) (range -74 (inc 74))))) ; 108
```

So we have 99 x vals and 108 y vals, about 10k possibilities. It should be pretty easy to brute force the checks.

```clojure
(defn hits-box? [vx vy x-min x-max y-min y-max]
  (let [[x y] (last (take-while (fn [[x y]] (and (<= x x-max) (>= y y-min)))
                                (map vector
                                     (trajectory-x 0 vx)
                                     (trajectory-y 0 vy))))]
    (and (>= x x-min)
         (<= y y-max))))

(time (count (for [vx (filter #(valid-vx? % 281 311) (range 0 (inc 311)))
             vy (filter #(valid-vy? % -74 -54) (range -74 (inc 74)))
             :when (hits-box? vx vy 281 311 -74 -54)]
         [vx vy]))) ; 1070
```

Bit unwieldy, but does the job in a not too heinous 300ms
