# Day 10: Syntax Scoring

[Link](https://adventofcode.com/2021/day/10)

Today is a syntax parsing problem. I forget how to do this every time.

The inputs are strings ('chunks') of brackets: `{([(<{}[<>[]}>{[]{[(<()>`. Each opening bracket should have a corresponding closing bracket of the same type.

## Part 1

Lines can be corrupted: they can have the wrong type of bracket:

```
{([(<{}[<>[]}>{[]{[(<()>
       ^    ^
 opening    expected ]
```

The task is to find the first incorrect closing bracket in each line of the input (if there is one), and score it: `)` 3, `]` 57, `}` 1197, `>` 25137.

So I think a way to do this would be to parse the string one character at a time. If the character is an opening bracket, push it on the stack (a list in Clojure), and if it's a closing bracket, pop the stack and make sure they match. If they don't, return the corrupted closing bracket. If it does, recur.

The functions would be something like:

* `detect-corruption :: string -> int` (0 if none)
* `matches? :: open-char, close-char -> bool` 
* `score :: char -> int`

``` clojure
(def brackets (apply hash-map "()<>{}[]")) ;; a map of opening to closing brackets
(def score {\) 3 \] 57 \} 1197 \> 25137})
(defn matches? [a b] (= b (brackets a))) ;; returns true if b is the closing bracket to a

(defn find-corruption
  ([input] (find-corruption '() input))
  ([stack [nxt & rst]]
  (cond ;; you've reached the end of the input with no corruption, return 0
        (not nxt) 0 

        ;; This is an opening bracket. Add it to the stack and recur
        ((set (keys brackets)) nxt)
        (recur (conj stack nxt) rst)

        ;; this closing bracket correctly closes the latest opening bracket
        ;; all is good, just continue with the rest of the stack and input
        (matches? (first stack) nxt)
        (recur (rest stack) rst)

        ;; The closing bracket does NOT match the latest opening bracket
        ;; This is a corruption! Return the score of the corrupted bracket
        :else (score nxt))))

(reduce + (map find-corruption input)) ; 387363
```

Simple enough.

## Part 2

**Incomplete** lines are missing some characters at the end of the line. You need to figure out what they are. Or more accurately, score the closing brackets you add. Since the closing brackets (the *completion string*) needed is just the stack I had in `find-corruption`, this should be easy.

So I can modify the above to return the autocompletion string if it reaches the end of the input.

The scoring system is:

* start with 0
* multiply the total score by 5
* add the score for the next character: 1,2,3,4 for `)]}>` respectively

```clojure
(defn autocompletion-string
  ([input] (autocompletion-string '() input))
  ([stack [nxt & rst]]
  (cond ;; if you get to the end, return the stack, but with the opening brackets
        ;; substituted with their closing counterparts
        ;; This is the autocompletion string
        (not nxt) (map brackets stack) 

        ((set (keys brackets)) nxt)
        (recur (conj stack nxt) rst)

        (matches? (first stack) nxt)
        (recur (rest stack) rst)

        :else (throw (ex-info "Corrupt Input" {})))))

(defn score-autocompletion [ac-string]
  (reduce (fn [a c] (+ (* a 5) (score-ac c)))
          0 ac-string))

(defn median [coll] (nth (sort coll) (/ (count coll) 2)))

(->> input
     (remove corrupt?)
     (map (comp score-autocompletion autocompletion-string))
     median) ; 4330777059
```

This is pretty nice I think. The only thing that bothers me is the duplicative code in autocompletion string and find corruption. I think this would be fine in a real program, since there's no guarantee the implementations will be so similar forever, and if you are trying to autocomplete, you should probably be assuming that there are no syntax errors and throwing if there are. 

But in the context of a puzzle it rankles. Maybe I could change autocompletion string to just nil out on a corrupt string. That way they could just be ignored, and I wouldn't have to explicitly filter them out.

```clojure
(defn autocompletion-string
  ([input] (autocompletion-string '() input))
  ([stack [nxt & rst]]
  (cond (not nxt) (map brackets stack)

        ((set (keys brackets)) nxt)
        (recur (conj stack nxt) rst)

        (matches? (first stack) nxt)
        (recur (rest stack) rst)

        :else nil))) ;; <- the only change

,,,

(->> input
     (keep autocompletion-string) ;; <- no need to filter out corruptions
     (map score-autocompletion)
     median)
```

Or, just have a more generic `parse` function which returns either an autocompletion string or a corrupted character. This introduces a bit of a dsl, but does remove the duplication.

```clojure
(defn parse ([input] (parse '() input))
  ([stack [nxt & rst]]
  (cond (not nxt) [:ac (map brackets stack)]

        ((set (keys brackets)) nxt)
        (recur (conj stack nxt) rst)

        (matches? (first stack) nxt)
        (recur (rest stack) rst)

        :else [:corruption nxt])))

(defn corrupt? [line]
  (let [[rslt out] (parse line)]
    (when (= rslt :corruption) ({\) 3 \] 57 \} 1197 \> 25137} out))))

(defn autocomplete? [line]
  (let [[rslt out] (parse line)]
    (when (= rslt :ac) (score-autocompletion out))))

(->> input (keep corrupt?) (apply +)) ; 387363
(->> input (keep autocomplete?) median) ; 4330777059
```

