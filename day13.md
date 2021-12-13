# Day 13: Transparent Origami

Another coordinate system. The input consists of two parts: the initial coordinates, and 'fold instructions'.

The critical operation is transforming a coordinate given a fold instruction: 

```
new-coord : canvas-size axis fold-line coord -> coord
fold : canvas axis fold-line -> canvas
```

In some cases, the coordinate will be unchanged. For example, if the fold instruction is `y=5`, anything with a fold less than 5 will be unaffected.

In some cases (e.g. where fold instruction is `y=5` and the y-coord is 5), the coordinate will just disappear

If the coordinate is 'below the fold', then the output coordinate will be more complicated. Consider a canvas of height 3, with a fold instruction of y=1. The following will be the result:

* `[x 0] -> [x 0]` (we can just call this nil, since it's not a 'new' coordinate)
* `[x 1] -> nil`
* `[x 2] -> [x 0]`

The formula for new-y will be:

```
new-canvas-size = (canvas-size - 1) / 2
(e.g. if cs = 5, new cs = 2)
new-unflipped-y = old-y - new-canvas-size 
  old-y = 4, new-y = 2
  old-y = 3, new-y = 1
new-y = new-canvas-size - new-unflipped-y
  old-y = 4, nu-y = 2, new-y = 0
  old-y = 3, nu-y = 1, new-y = 1

Together:
new-y = ((canvas-size - 1) / 2) - (old-y - ((canvas-size - 1) / 2))
new-y = ((canvas-size - 1) / 2) - old-y + ((canvas-size - 1) / 2)
new-y = canvas-size - 1 - old-y
```

Note this assumes the canvas will always be divided in half. This may not be valid, and could cause us some problems later.

Re: data structures: The coordinates themselves are binary (either `#` or `.`), so we can represent them with a set. The axis we can represent with 0 (for x) or 1 (for y), since this will let us index into coordinates.

A 'fold' consists of the following operations:

* find the 'new' coordinates of every coord in the canvas
* union that with the canvas (this reflects the 'transparency' feature)
* remove all coordinates which are 'below the fold'

Putting all that together we get the following:

```clojure
(defn new-coord [canvas-size axis fold-line coord]
  (when (> (get coord axis) fold-line)
    (update coord axis #(- (get canvas-size axis) % 1))))

(defn fold [canvas axis fold-line]
  (->> canvas
       (keep #(new-coord (canvas-size canvas) axis fold-line %))
       (set/union canvas)
       (remove #(>= (get % axis) fold-line))
       set))
```

From here we just need to parse the input.

```clojure
(defn parse [input-str]
  (let [[coords instr] (str/split input-str #"\n\n")]
    [(set (map vec (partition 2 (u/extract-ints coords))))
     (map vector (map #(if (= "x" %) 0 1) (re-seq #"[xy]" instr))
                 (u/extract-ints instr))]))

(def example (parse (slurp "resources/day13example.txt")))

(print-canvas (apply reduce fold example))
; "#####\n
;  #...#\n
;  #...#\n
;  #...#\n
;  #####"
```

Trying this on the actual input, though, gives nonsense. So I've missed something. I think I know what it is: When I fold a canvas, the output is a set of coordinates which indicates the 'dots'. The assumption was that I can get the canvas size by finding the maximum coordinate of the dot. But that's not the case: there might be empty space at the end of the canvas. Like in the final state of the example, there are two empty lines at the end.

```
#####
#...#
#...#
#...#
#####
.....
.....
```

So I need to store the canvas size separately from the dots. My canvas data structure will now be a map with keys `:dots` and `:size`. Another problem is detecting the initial size of the canvas. I think the 'canonical' way to do this is to look at the first x fold, and first y fold, double them, and add one.

```clojure
(defn initial-canvas-size [folds]
  [(inc (* (second (first (drop-while #(not= 0 (first %)) folds))) 2))
   (inc (* (second (first (drop-while #(not= 1 (first %)) folds))) 2))])

(defn parse [input-str]
  (let [[coords instr] (str/split input-str #"\n\n")
        parsed-coords (set (map vec (partition 2 (u/extract-ints coords))))
        parsed-instr (map vector (map #(if (= "x" %) 0 1) (re-seq #"[xy]" instr))
                                 (u/extract-ints instr))]
    [{:dots parsed-coords
      :size (initial-canvas-size parsed-instr)}
     parsed-instr]))

,,,

(defn new-dots [dots size axis fold-line]
  (->> dots
       (keep #(new-coord size axis fold-line %))
       (set/union dots)
       (remove #(>= (get % axis) fold-line))
       set))

(defn new-canvas-size [current axis]
  (update current axis #(/ (dec %) 2)))

(defn fold [canvas [axis fold-line]]
  (-> canvas
      (update :dots new-dots (:size canvas) axis fold-line)
      (update :size new-canvas-size axis)))
```

Some pretty ugly stuff in there, ripe for a tidy. But it gets the right answer to parts 1 and 2:

```clojure
(count (:dots (fold (first input) (first (second input)))))
; 737

(print-canvas (apply reduce fold input)) 
; "####.#..#...##.#..#..##..####.#..#.###..
;  ...#.#..#....#.#..#.#..#.#....#..#.#..#.
;  ..#..#..#....#.#..#.#..#.###..####.#..#.
;  .#...#..#....#.#..#.####.#....#..#.###..
;  #....#..#.#..#.#..#.#..#.#....#..#.#....
;  ####..##...##...##..#..#.#....#..#.#...."
```

## Final code after (very minor) refactor

```clojure
(defn initial-canvas-size [folds]
  [(inc (* (second (first (drop-while #(not= 0 (first %)) folds))) 2))
   (inc (* (second (first (drop-while #(not= 1 (first %)) folds))) 2))])

(defn parse [input-str]
  (let [[coords instr] (str/split input-str #"\n\n")
        parsed-instr  (map vector (map #(if (= "x" %) 0 1) (re-seq #"[xy]" instr)) (u/extract-ints instr))]
    [{:dots (set (map vec (partition 2 (u/extract-ints coords))))
      :size (initial-canvas-size parsed-instr)}
     parsed-instr]))

(def input (parse (slurp "resources/day13input.txt")))

(defn new-coord [size axis fold-line coord]
  (when (> (get coord axis) fold-line)
    (update coord axis #(- (get size axis) % 1))))

(defn new-dots [dots size axis fold-line]
  (set/union (set (remove #(>= (get % axis) fold-line) dots))
             (set (keep #(new-coord size axis fold-line %) dots))))

(defn new-canvas-size [current axis] (update current axis #(/ (dec %) 2)))

(defn fold [canvas [axis fold-line]]
  (-> (update canvas :dots new-dots (:size canvas) axis fold-line)
      (update        :size new-canvas-size axis)))

(count (:dots (fold (first input) (first (second input)))))
; 737

(println (u/print-coord-set (:dots (apply reduce fold input))))
; "####.#..#...##.#..#..##..####.#..#.###..
;  ...#.#..#....#.#..#.#..#.#....#..#.#..#.
;  ..#..#..#....#.#..#.#..#.###..####.#..#.
;  .#...#..#....#.#..#.####.#....#..#.###..
;  #....#..#.#..#.#..#.#..#.#....#..#.#....
;  ####..##...##...##..#..#.#....#..#.#...."
```

## Eureka moment: You don't need to store the canvas size

So a worked myself into a corner with my implementation of `new-coord` being `new = canvas-size - 1 - old`. That's why I had to store the canvas size. But you don't need to do that at all, because the canvas size is `(2 * fold-line) + 1`. Fold line is already part of the input, so instead of using canvas size you can just use `new = (2 * fold-line) - old`. That gets rid of the need to store the canvas size, and so allows me to simplify the code a _lot_, to just 13 lines of code.

```clojure
(defn parse [input-str]
  (let [[coords instr] (str/split input-str #"\n\n")]
    [(set (map vec (partition 2 (u/extract-ints coords))))
     (map vector (map #(if (= "x" %) 0 1) (re-seq #"[xy]" instr)) (u/extract-ints instr))]))

(def input (parse (slurp "resources/day13input.txt")))

(defn new-coord [axis fold-line coord]
  (when (> (get coord axis) fold-line)
    (update coord axis #(- (* 2 fold-line) %))))

(defn fold [dots [axis fold-line]]
  (set/union (set (remove #(>= (get % axis) fold-line) dots))
             (set (keep #(new-coord axis fold-line %) dots))))

(count (fold (first input) (first (second input))))
(println (u/print-coord-set (apply reduce fold input)))
```
