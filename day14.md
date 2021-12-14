# Day 14: Extended Polymerization

You start with a string, and a list of rules in the form `CH -> B`. This rule implies that `B` should be inserted between `C` and `H` wherever it occurs.

This will obviously grow extremely quickly.

## Part 1

**What do you get if you take the quantity of the most common element and subtract the quantity of the least common element after 10 steps?**

The brute force for this would be pretty simple I think. Store the replacements as `{[\C \H] [\C \H \B]}`, run a window over the input, and mapcat the replacements.

The problem is going to be efficiency. After 10 steps with a small input, you've got maybe 5k characters. No doubt part 2 will have you calculate 100 steps, or 1 million or something. So is there a shortcut? This feels a lot like the Lantern fish problem, maybe I can use a matrix. But I don't see how that would track the adjacencies.

Well I guess as a start, just try the brute force method to get the wheels turning.

Yep, pretty easy for part 1:

```clojure
(defn parse-rule [line]
  (let [[[a b] [c]] (re-seq #"[A-Z]+" line)]
    [[a b] [a c b]]))

(def input-rules (into {} (map parse-rule (drop 2 (str/split-lines (slurp "resources/day14input.txt"))))))
(def input-string (first (str/split-lines (slurp "resources/day14input.txt"))))
(defn first-and-last [coll] [(first coll) (last coll)])

(defn step [rules input]
  (conj (vec (mapcat #(butlast (get rules % %)) (partition 2 1 input))) (last input)))

(time (->> (iterate #(step input-rules %) input-string)
           (take (inc 10))
           (last)
           (frequencies)
           (map second)
           (sort >)
           (first-and-last)
           (apply -)))
; 2345
```

## Part 2

Is just 40 steps. I let it run for about 15 mins and no dice, unsurprisingly. So what's the trick?

I tried messing around with matrices for a bit, but didn't notices any real patterns. One thing I did notice: the rules are "complete", in the sense the every single potential pair has a rule. Probably important. It also seems like there are no cases where a double letter input results in an output of that same letter. But I'm less sure that's important.

Consider a trivial example with two elements and four rules: 

```
AA->B
AB->B
BA->A
BB->A
```

A pair `AA` will generate 2 new pairs: `AB` `BA`. These will generate `AB: AB, BB` and `BA: BA AA`

```
   A  B         A  B         A  B
A  1  0  =>  A  0  1  =>  A  1  1
B  0  0      B  1  0      B  1  1
```

How about we represent the rules as a matrix. The size will be `(n^2)x(n^2)`, where `n` is the number of elements.

```
   AA AB BA BB
AA  0  1  1  0
AB  0  1  0  1
BA  1  0  1  0
BB  0  1  1  0
```

Now we represent the input as a vector of pairs, and multiply the vector by the matrix successively: 

```
    start step1  step2  step3  step4
AA    1     0      1      1      3
AB    0     1      1      3      5
BA    0     1      1      3      5
BB    0     0      1      1      3
```

This was accomplished using the following matrix multiplication code:

```clojure
(defn dot-product [v1 v2] (apply + (map * v1 v2)))

(defn matrix-mult [v m] (map #(dot-product v %) (apply map vector m)))

(let [rules [[0 1 1 0] [0 1 0 1] [1 0 1 0] [0 1 1 0]]]
  (zipmap ["AA" "AB" "BA" "BB"] 
          (-> [1 0 0 0]
              (matrix-mult rules)
              (matrix-mult rules)
              (matrix-mult rules)
              (matrix-mult rules)))) 
; {"AA" 3, "AB" 5, "BA" 5, "BB" 3}
```

This agrees with the result of the code from part 1:

```clojure
(let [rules (into {} (map parse-rule ["AA->B" "AB->B" "BA->A" "BB->A"]))]
  (->> "AA"
       (step rules) ; {(\A \B) 1, (\B \A) 1}
       (step rules) ; {(\A \B) 1, (\B \B) 1, (\B \A) 1, (\A \A) 1}
       (step rules) ; {(\A \B) 3, (\B \B) 1, (\B \A) 3, (\A \A) 1}
       (step rules) ; {(\A \B) 5, (\B \B) 3, (\B \A) 5, (\A \A) 3}
       (count-pairs))) 
```

After this, there are 2 challenges: get the frequency of the characters out of this, and turn the input into matrix/vectors. Take the last first:

```clojure
(defn parse-rule2 [line]
  (let [[in out] (re-seq #"[A-Z]+" line)]
    [[in (str (first in) out)] [in (str out (last in))]] ))

(defn matrices [init-str rules-lines]
  (let [rules (mapcat parse-rule2 rules-lines)
        pos-map (zipmap (sort (set (map first rules))) (range))
        init-v (vec (repeat (count pos-map) 0))
        init-m (vec (repeat (count pos-map) init-v))]
    [(reduce (fn [v i] (update v (pos-map i) inc))
             init-v (map #(apply str %) (partition 2 1 init-str)))
     (reduce (fn [m [in out]] (assoc-in m [(pos-map in) (pos-map out)] 1))
             init-m rules)
     pos-map]))

(matrices "AA" ["AA->B" "AB->B" "BA->A" "BB->A"])
; [[1 0 0 0]
;  [[0 1 1 0] [0 1 0 1] [1 0 1 0] [0 1 1 0]]
;  {"AA" 0, "AB" 1, "BA" 2, "BB" 3}]
```

Bit ugly, but does the job.

OK, so how to get from this to a count of the elements?

Using our `AB` Example, we end up after 3 iterations with the following

``` clojure
; output string
"ABBABAABA"
; character frequency
{\A 5, \B 4}
; pair frequency
{"AB" 3, "BB" 1, "BA" 3, "AA" 1}
```

The last is the results of our matrix operations. 2nd is what we want to get to. Start with A. For each pair `AB`, `BA`, we can count 1 (so, 6). For `AA`, it's in there twice, so count 2, for a total of 8. But for each pair, the `A` is going to be the start or end of another pair _except_ the first and last chars. So add them, then divide by 2. 

```
ABA -> pairs: AB BA 
-> pair freq: {AB 1, BA 1} 
-> counted chars {A 2, B 2} 
-> add start, end {A 4 B 2}
-> div 2 {A 2 B 1}

ABBAA -> pairs: AB BB BA AA
-> pair freq: {AB 1 AA 1, BB 1, BA 1} 
-> counted chars {A 4, B 4} 
-> add start, end {A 6, B 4}
-> div 2 {A 3, B 2} 

ABBABAABA -> pairs AB BB BA AB BA AA AB BA
-> pair freq: {AB 3, BB 1, BA 3, AA 1}
-> counted chars {A 8, B 8} 
-> add start, end {A 10, B 8}
-> div 2 {A 5, B 4} 
```

Add all of this together and you get the rather heinous:

```clojure
(let [input (str/split-lines (slurp "resources/day14input.txt"))
      [init-v rules-mat pos-map] (matrices (first input) (drop 2 input))]
  (->> (iterate #(matrix-mult % rules-mat) init-v)
       (take (inc 40))
       (last)
       (zipmap (sort (keys pos-map)))
       (reduce count-chars {})  
       (inc1 (ffirst input))
       (inc1 (last (first input)))
       (u/map-vals #(/ % 2))
       (map second)
       (sort >)
       (first-and-last)
       (apply -))) ; 2432786807053
```

## Code after a few rounds of refactoring

```clojure
(defn parse-rule [line]
  (let [[in out] (re-seq #"[A-Z]+" line)]
    [[in (str (first in) out)] [in (str out (last in))]] ))

(let [[init-str rules] (str/split (slurp "resources/day14input.txt") #"\n\n")]
  (def input-str init-str)
  (def rules-parsed (mapcat parse-rule (str/split-lines rules))))

(defn matrix-from [in pos-map [r c]]
  (reduce (fn [m xs] (update-in m (map pos-map xs) inc))
          (u/make-matrix r c) in))

(defn repeated-mat-mult [n v m] (last (take (inc n) (iterate #(u/matrix-mult % m) v))))
(defn f [A [[a b] c]] (-> A (update a (fnil + 0) c) (update b (fnil + 0) c)))
(defn count-chars-from-pairs [pair-map] (u/update-vals (reduce f {} pair-map) / 2))
(defn most-least-diff [m] (apply - (u/first-and-last (sort > (map second m)))))

(defn calc [n input-str rules pos-map]
  (let [pairs (count pos-map)
        input-vector (matrix-from (map #(vector (apply str %)) (partition 2 1 input-str)) pos-map [1 pairs])
        rules-matrix (matrix-from rules pos-map [pairs pairs])]
    (->> (repeated-mat-mult n input-vector rules-matrix)
         (zipmap (sort (keys pos-map)))
         (merge-with + {(u/first-and-last input-str) 1})
         (count-chars-from-pairs)
         (most-least-diff))))

(def pos-map (zipmap (sort (set (apply concat rules-parsed))) (range)))

(calc 10 input-str rules-parsed pos-map)
; 2345

(time (calc 40 input-str rules-parsed pos-map))
; 132ms
; 2432786807053
```
