# Day 8: Seven Segment Search

An input line is a series of strings, a `|` delimiter, and four output strings.

A string decodes to a digit, but the letters are scrambled within each line. You need to de-scramble them to figure out which string corresponds to which number, then determine what four digits the output correspond to.

The letters are scrambled though. You have to decode them.

Here are the unscrambled digit codes:

``` clojure
(def segments
  {1 (set "cf")
   7 (set "acf")
   4 (set "bcdf")
   5 (set "abdfg")
   2 (set "acdeg")
   3 (set "acdfg")
   9 (set "abcdfg")
   0 (set "abcefg")
   6 (set "abdefg")
   8 (set "abcdefg")})
```

This is a sample input

`acedgfb cdfbe gcdfa fbcad dab cefabd cdfgeb eafb cagedb ab |
cdfeb fcadb cdfeb cdbaf`

This has a value "ab", which, since 1 is the only digit represented by a 2 length string, must correspond to 1

**Part 1: In the output values, how many times do digits 1, 4, 7, or 8 appear?**

The answer to part 1 is simple, because the encodings for 1,4,7,8 all have unique lengths - 2,4,3 and 7 respectively. So part 1 should be a simple case of filtering the outputs for those lengths.

``` clojure
(defn parse-line [line]
  (map #(re-seq #"[a-z]+" %) (str/split line #"\|")))

(->> input
     (map parse-line)
     (mapcat last)
     (filter #(#{2 4 3 7} (count %)))
     count)
;; => 493
```
**Part 2: What do you get if you add up all of the output values?**

Now for the tricky part: How do you deduce the actual mappings?

Part 1 seems to give us the hint that we use 1 4 7 and 8 to give us a hint. Actually not 8: since it contains all letters, it's effectively information free.

But if we take 1 (true encoding "cf") in the above example, the scrambled encoding is "ab". That means c and f are both encoded as either a or b.

So I'm thinking we do a sort of elimination thing.
