# Day 12: Passage pathing

Looks like a graph problem.

## Part 1

Find the number of distinct paths that start at start and end at end. Some nodes (those in lower-case) must only be visited at most once once, some can be visited multiple times.

You solve this type of problem with a depth first search. But that required a DAG, and I don't think this is a strict DAG. My guess it is _implicitly_ a DAG due to the constraints on visiting some nodes only once.

For example, if you had a graph like:

```
   END
    |
    A-B-[other stuff]
```

Then there are an infinite number of paths to END, because you can move back and forth between A and B as many times as you like.

I think my first approach is going to be to try a DFS for finding all paths, trying to build in the 'visit once' constraint and see what happens.

So DFS. The basic idea, stated in an imperative way, is that you walk a DAG until you get to a leaf. Then you backtrack until you find a node which has children you haven't visited yet and repeat the process.

Stated in a more recursive way, the DFS of the graph starting from node `X`, is `X` plus the DFS of all the children of `X` (or, if `X` has no children, `X`).

The return value here is either a sequence or a set. I'm using a sequence here since it preserves the order and lets us see the order, but it means there will be duplicates.

Our termination condition is: if `X` has no children, return a sequence containing just `X`. `(list x)`

Our recur expression is "add `X` onto the DFS of all the children of `X`": `(cons x (mapcat #(dfs graph %) children))`

```clojure
(defn dfs [graph node]
   (let [nxt (sort < (children graph node))] ;; sort is superfluous, but makes examples simpler
     (if (empty? nxt) (list node)
       (cons node (mapcat #(dfs graph %) nxt)))))
```

Finding all paths between two points in a DAG is a minor modification to this: All paths between points `X` and `Y` in a DAG are 

* If `X` _is_ `Y`, the only path is a sequence just containing `X`. But we're returning a sequence of paths, so we need to do `(list (list x))`.
* Otherwise, the paths between `X` and `Y` are all the paths between the `X`s _children_ and `Y`, with `X` tacked on the end.

```clojure
(defn all-paths-between [graph from to]
  (if (= from to) (list (list from))
    (->> (children graph from)
         (mapcat #(all-paths-between graph % to) )
         (map #(cons from %)))))
```

One problem with this algorithm is that it's not 'path aware'. That is, when you recursively call it, there's no opportunity to check the path. This is important for the AOC problem, because you need to check where you've been to see if you can go to the next node.

So here's a path aware version. Very similar, but it keeps track of the path it used to get to this point. The first element of the path (assuming this is a fifo / linked list) is the 'current node'.

```clojure
(defn path-aware-all-paths-between [graph path to]
  (let [nxt (children graph (first path))]
    (if (= (first path) to) (list path)
      (mapcat #(path-aware-all-paths-between graph (conj path %) to) nxt))))
```

Now, instead of children, we can calculate `nxt` with a function that is more discriminating (`next-nodes` in the below)

Here's the part 1 code:

```clojure
(defn children [graph node] (set (keep #(when (= node (first %)) (second %)) graph)))
(defn lower-case? [string] (= (str/lower-case string) string))

(defn next-nodes [graph this-node path]
  (set/difference (children graph this-node)
                  (set (filter (comp lower-case? name) path))))

(defn make-edges [line] ((juxt identity reverse) (str/split line #"-")))

(defn all-paths
  ([from to graph] (all-paths (list from) to 0 graph))
  ([path to depth graph]
   (let [nxt (next-nodes graph (first path) path)]
     (cond (> depth 100) (throw (ex-info "depth > 10" {}))
           (= (first path) to) (list path)
           :else (mapcat #(all-paths (conj path %) to (inc depth) graph) nxt)))))

(->> (slurp "resources/day12input.txt")
     (str/split-lines)
     (mapcat make-edges)
     (all-paths "start" "end")
     count)
;; => 4241
```

## Part 2:

We are now able to visit a single lower-case node (except "start" and "end") twice. I think we just need to add an even more discriminating `next-nodes` function.

A quick refactor so `all-paths` can be passed any next-nodes function, and then add the new next-nodes rules. `no-lc-revisits` is the `next-nodes` rule from part 1.

```clojure
(defn all-paths
  ([next-fn from to graph] (all-paths next-fn (list from) to 0 graph))
  ([next-fn path to depth graph]
   (let [nxt (next-fn graph (first path) path)]
     (cond (> depth 100) (throw (ex-info "depth > 10" {}))
           (= (first path) to) (list path)
           :else (mapcat #(all-paths next-fn (conj path %) to (inc depth) graph) nxt)))))

(defn no-lc-revists [graph this-node path]
  (set/difference (children graph this-node)
                  (set (filter lower-case? path))))

(defn one-lc-revist [graph this-node path]
  (if ((set (vals (frequencies (filter lower-case? path)))) 2)
    (no-lc-revists graph this-node path)
    (disj (children graph this-node) "start")))

(time (->> (slurp "resources/day12input.txt")
           (str/split-lines)
           (mapcat make-edges)
           (all-paths no-lc-revisits "start" "end")
           count))
;; Elapsed time: 210.555148 msecs
;; => 4241

(time (->> (slurp "resources/day12input.txt")
           (str/split-lines)
           (mapcat make-edges)
           (all-paths one-lc-revisit "start" "end")
           count))
;; => Elapsed time: 8972.176427 msecs
;; => 122134
```

Part 2 is _crazy slow_, but good enough I think: I like that the only difference is the different function being passed.

## Full code (after a couple of rounds of refactoring)

```clojure
(def graph (mapcat #((juxt identity reverse) (str/split % #"-")) (str/split-lines (slurp "resources/day12input.txt"))))

(defn children [graph node] (set (keep #(when (= node (first %)) (second %)) graph)))
(defn lower-case? [string] (= (str/lower-case string) string))

(defn with-no-lc-revisits [graph node path]
  (set/difference (children graph node) (set (filter lower-case? path))))

(defn with-one-lc-revisit [graph node path]
  (if ((set (vals (frequencies (filter lower-case? path)))) 2)
    (with-no-lc-revisits graph node path)
    (disj (children graph node) "start")))

(defn all-paths 
  ([next-fn graph] (all-paths next-fn '("start") "end" graph))
  ([next-fn path to graph]
   (if (= (first path) to) (list path)
       (mapcat #(all-paths next-fn (conj path %) to graph) (next-fn graph (first path) path)))))

(comment
  (time (count (all-paths with-no-lc-revisits graph)))
  ;; Elapsed time: 210.555148 msecs
  ;; => 4241

  (time (count (all-paths with-one-lc-revisit graph)))
  ;; => Elapsed time: 8972.176427 msecs
  ;; => 122134
  )
```
