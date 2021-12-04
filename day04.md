# Day 4: Giant Squid (Bingo)

Today we're playing Bingo!

The input has:

* A list of comma separated integers on the first row - the draws from the bingo machine
* A bunch a grids of 5x5 integers - 'boards'

A bingo board 'wins' when you draw enough numbers to complete a row or column on a board.

The score is the sum of the uncompleted numbers on a finished board, times the last number drawn which wins the board.

## Part 1: Find the winning board and calculate its score.

I can't think of any tricks here: just process the numbers one at a time and tick them off on each board. 

Or, have the ability to run each board and get it's 'time to win' number. This would mean you don't have to maintain all the states of the boards in a big structure, which could get unwieldy.

So what do we need to be able to do here?

* `time-to-win :: board, numbers, max -> int!nil` (the max is so we don't waste time processing when we know this board isn't the overall winner)
* `has-won? :: board -> bool` - will need to be able to look at rows and columns and see if they are completed
* `mark :: board, number -> board`

This is assuming we're tracking the state of the board. 

Actually you don't really need to do this at all: you just need `has-won? :: board, drawn-numbers -> bool`. That would remove the need for a `mark` function.

The implementation of this is something like:

> generate the rows and columns of the board. 
>
> If the set of numbers in any row or column are a subset of the set of numbers drawn, the board has won.

If we have our boards structured as arrays-of-arrays, then the rows are simply the rows, and the columns are the rows of the pivoted data structure.

Let's try!

_About half an hour later:_ Fun!

The first thing is to parse the input. Extracting the draws from the first line is trivial. Parsing the boards is more interesting, and we get to use some old-school non-tail recursion: _To parse all the boards, cons this board onto the rest of the boards_! I'm feeling very SICP about the whole thing.

``` clojure
(defn parse-boards [lines]
  (let [[board-lines remaining]  (split-with #(not= % "") lines)]
    (when (not-empty board-lines)
      (cons (mapv extract-ints board-lines) 
            (parse-boards (rest remaining))))))
```

`has-won?` is pretty much as described in the spec above. The `pivot` here turns the rows into columns.

``` clojure
(defn has-won? [board draws]
  (some #(set/subset? (set %) (set draws)) (into board (pivot board))))
```

`has-won?` is used to find the *time to win* of each board, and *scoring the boards*. In the first, you `take-while not-won` and count the steps, and in the second you `drop-while not-won` and the first result is the sequence of drawn numbers that resulted in the win. 

Scoring the quickest board is just a composition of finding the quickest board and scoring it.

Note that what we're operating on in the below code is a _cumulative sequence_ of draws. The first element is the first draw only, the second is the first two draws, etc. This is obtained by `reductions`, which for some reason I'm always happy when I get to use.

``` clojure
(defn time-to-win [board cumulative-draws]
  (count (take-while #(not (has-won? board %)) cumulative-draws)))

(defn quickest-board [boards cumulative-draws]
  (first (sort-by #(time-to-win % cumulative-draws) boards)))

(defn score-winning-state [board cumulative-draws]
  (let [win-draws (first (drop-while #(not (has-won? board %)) cumulative-draws))]
    (->> (set win-draws)
         (set/difference (set (flatten board)))
         (apply +)
         (* (last win-draws)))))

(defn score-of-quickest-board [boards draws]
  (let [cumulative-draws (reductions conj [] draws)]
    (-> boards
        (quickest-board cumulative-draws)
        (score-winning-state cumulative-draws))))

(time (score-of-quickest-board boards draws))
;; Elapsed time: 3387.181187 msecs
;; => 31424
```

The result is _very slow_. Partly I think this is because there's a lot of set operations in there, which are not so speedy. Second, there's no short circuiting of the time to win: it eagerly calculates the whole thing regardless of if we already know it's not a winner because we have something that wins in less draws. Let's see if we can fix that...

First, make a short-circuiting time-to-win:

``` clojure
(defn time-to-win2 [board cumulative-draws stop-at]
  (reduce (fn [count draws]
            (cond (> count stop-at)      (reduced nil)
                  (has-won? board draws) (reduced count)
                  :else                  (inc count)))
          0
          cumulative-draws))

(= (map #(time-to-win % (reductions conj [] draws)) boards)
   (map #(time-to-win2 % (reductions conj [] draws) 100) boards))
;; => true
```

Next make quickest board utilize that short circuit:

``` clojure
(defn quickest-board2 [boards cumulative-draws]
  (first (reduce (fn [[best-board best-ttw] new-board]
            (let [new-ttw (or (time-to-win2 new-board cumulative-draws best-ttw) inf)]
              (if (< new-ttw best-ttw)
                [new-board  new-ttw]
                [best-board best-ttw])))
          [nil inf]
          boards)))

(= (quickest-board boards (reductions conj [] draws))
   (quickest-board2 boards (reductions conj [] draws)))
;; => true

(time (quickest-board boards (reductions conj [] draws)))
;; Elapsed time: 3637.119129 msecs
(time (quickest-board2 boards (reductions conj [] draws)))
;; Elapsed time: 155.435408 msecs
```

Very good improvement! The top function - `score-of-quickest-board` has basically the same performance: ~150ms

You do have that double-reduce, which is not beautiful. But given that I've working on this for about an hour now, I think it's good enough.

The nil-punning is also maybe a bit odd. `time to win` returns `nil` if the `stop-at` is exceeded. I think this makes sense for the context. This then gets or'd to infinity and used in a `<` comparison. I don't think I actually need to do that: the existence of a non-nil return value from the `time to win` indicates that it is the best result yet seen, so I can just use an if-let:

``` clojure
(defn quickest-board [boards cumulative-draws]
  (first (reduce (fn [[best-board best-ttw] new-board]
                   (if-let [new-ttw (time-to-win new-board cumulative-draws best-ttw)]
                     [new-board  new-ttw]
                     [best-board best-ttw]))
                 [nil inf]
                 boards)))
```

OK, definitely enough fiddling now - move on to part 2.

## Part 2

Find the **losing** board and calculate its score.

Just the same thing as part 1, but with a 'longest win'. Since this means there is no short circuiting, it basically undoes all the optimization I did! If I'd just kept sorted the whole thing I could've just taken the last element of that same sort. Premature optimization is truly a curse.

``` clojure
(defn slowest-board [boards cumulative-draws]
  (first (reduce (fn [[worst-board worst-ttw] new-board]
                   (let [new-ttw (time-to-win new-board cumulative-draws inf)]
                     (if (> new-ttw worst-ttw)
                       [new-board   new-ttw]
                       [worst-board worst-ttw])))
                 [nil 0]
                 boards)))

(defn score-of-slowest-board [boards draws]
  (let [cumulative-draws (reductions conj [] draws)]
    (-> boards
        (slowest-board cumulative-draws)
        (score-winning-state cumulative-draws))))

(time (score-of-slowest-board boards draws))
;; Elapsed time: 378.131766 msecs
;; => 23042
```

Actually it's still much faster than before (I've no idea why), so maybe not a waste. But it's still much more code, whereas before I could've just used what I had from part 1. 

## Refactor

I also think it's less beautiful than what I had before, which was very declarative of the problem. "Sort these by time-to-win and take the fastest/slowest", right there in the code (see below block). With the implementation I have to go down a level and start fiddling with `reduce`.

``` clojure
(first (sort-by #(time-to-win % cumulative-draws) boards))
```

The reason I got rid of this is because I thought the slowness was due to it not short circuiting on the time-to-win calculation. But since part 2 _doesn't_ short circuit, and is just about as fast, that can't be it.

So why is the plain sort-by so much slower than the reduce? Maybe the sort algorithm is calling `time-to-win` more than once on each board, where the reduce solution is calling it only once? So I think if I precalc the time to win and sort by that, I should avoid that problem:

``` clojure
(defn sort-by-ttw [boards cumulative-draws]
  (->> boards
       (map (juxt #(time-to-win % cumulative-draws inf) identity))
       (sort-by first)
       (map second)))

(time (let [cumulative-draws (reductions conj [] draws)
            sorted-boards (sort-by-ttw boards cumulative-draws)]
          [:quickest
           (score-winning-state (first sorted-boards) cumulative-draws)
           :slowest
           (score-winning-state (last sorted-boards) cumulative-draws)]))
  ;; Elapsed time: 347.927555 msecs
  ;; => [:quickest 31424 :slowest 23042]
```

Since this means I'm effectively not using the short-circuiting anyway, I can get rid of the reduce in time to win, and go back to the nice implementation I had at the start.

``` clojure
(defn time-to-win [board cumulative-draws]
  (count (take-while #(not (has-won? board %)) cumulative-draws)))
```

That brings me down to a sub-50 line solution, and pretty clean implementations for all the functions.

I didn't do enough research into the cause of the slow implementation, which sent me down a bad path. I assumed it was due to the lack of short-circuiting, when really it was on the implementation of my sorting.

My lesson from today is _when something is too slow, verify the cause of the slowness before you optimize_

## Full solution

The [source file](./clojure/src/aoc2021/day04.clj).

``` clojure
(defn extract-ints [coll] (mapv #(Long/parseLong %) (re-seq #"\d+" coll)))
(defn pivot [xs] (apply map vector xs))

(defn parse-boards [lines]
  (let [[board-lines remaining]  (split-with #(not= % "") lines)]
    (when (not-empty board-lines)
      (cons (mapv extract-ints board-lines) (parse-boards (rest remaining))))))

(def input (str/split-lines (slurp "resources/day04input.txt")))
(def draws (extract-ints (first input)))
(def boards (parse-boards (drop 2 input)))

(defn has-won? [board draws]
  (some #(set/subset? (set %) (set draws)) (into board (pivot board))))

(defn time-to-win [board cumulative-draws]
  (count (take-while #(not (has-won? board %)) cumulative-draws)))

(defn sort-by-time-to-win [boards cumulative-draws]
  (->> boards
       (map (juxt #(time-to-win % cumulative-draws) identity))
       (sort-by first)
       (map second)))

(defn score-winning-state [board cumulative-draws]
  (let [win-draws (first (drop-while #(not (has-won? board %)) cumulative-draws))]
    (->> (set win-draws)
         (set/difference (set (flatten board)))
         (apply +)
         (* (last win-draws)))))

(time (let [cumulative-draws (reductions conj [] draws)
            sorted-boards (sort-by-time-to-win boards cumulative-draws)]
        [:quickest (score-winning-state (first sorted-boards) cumulative-draws)
         :slowest  (score-winning-state (last sorted-boards) cumulative-draws)]))
;; Elapsed time: 367.260945 msecs
;; => [:quickest 31424 :slowest 23042]
```

