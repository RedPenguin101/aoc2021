# Day 3: Binary Diagnostic

* [Clojure source](./clojure/src/aoc2021/day03.clj)

## Part 1

We're given a list of binary numbers to decode.

The _gamma_ is a binary number where each bit is the most common value of the input bits in that position.

The _epsilon_ uses the _least_ common bit.

Multiply the gamma and epsilon together.

It would be simply enough to treat each bit as a character and count the frequencies. But since this is a binary problem, there must be a way to do this using binary operations.

The problem does say what to do in case of a tie, so I'd assume there aren't any.

I feel like this is something to do with checksums and error detection?

The counting thing is hard though, since you only have one bit.

Or maybe there's a trick in that gamma and epsilon are inverse: wherever gamma has a 1, epsilon has a 0.

About 20 mins later: Couldn't figure out a smart way to do it, so here's a stupid way:

``` clojure
(defn pivot [xs] (apply map vector xs))
(defn least-most-common [xs] (map first (sort-by val (frequencies xs))))

(->> input
     pivot
     (map least-most-common)
     pivot
     (map #(Long/parseLong (apply str %) 2))
     (apply *))
```

Not the most elegant in the world, but probably not too bad.

I don't like the double pivot, and I played around with some HOFs that would 'pivot wrap' another function - i.e. pivot, apply function, unpivot. But I couldn't get it to work well.

Can't help feeling like I missed a trick somewhere though. One thing I saw is that, if you have the bits as digits, you can take the mean and test if it's gt or lt 0.5 (i.e. Round it). That might be nice in a very numerical language (APL, R), but here it doesn't seem much better. Probably when I do Zig that will be a better way.

## Part 2

The _OGR_ and _COSR_ both use a bitwise filtering process. Each measure has a _bit criteria_ which is used in the bitwise filter. This is applied successively until only 1 value remains.

For the OGR, the bit criteria is: for the first bit, find the _most common bit_, and exclude anything that isn't that bit for that position. Then move on to the second bit. Draws are won by 1.

The COSR is the same, but with _least common_. Draws are won by 0.

This took about half an hour to solve, maybe another half hour to polish.

The first important thing is that you can have _draws_ now. So the `least-most-common` function from above is no longer enough. I wrote a new function, `common`, which given a sequence will return either the least or most common, based on what you ask for, and will handle the tie-breaking.

``` clojure
(defn common [most? xs]
  (let [freqs (frequencies xs)]
    (if (apply = (vals freqs)) (first (str most?))
        (nth (map first (sort-by val freqs)) most?))))
```

`most?` here is a value of 1 (most) or 0 (least), and it's used in two ways: first, if there's a tie, it's coerced to a character and used as the tie break value. Second, it's used as an index to return the least or most common character. This is maybe a bit too clever for its own good, and certainly means it's not able to be reused in other contexts.

Next, implement the bitwise filter using a simple recursion over the filtering, with a count of 1 being the terminator. Again, `most?` is a 1 or 0, depending on whether you want to find the most or least common value.

``` clojure
(defn bitwise-filter [bins idx most?]
  (if (= 1 (count bins)) (first bins)
      (let [pos #(nth % idx)
            test (common most? (map pos bins))]
        (recur (filter #(= test (pos %)) bins)
               (mod (inc idx) (count (first bins)))
               most?))))

(->> (map #(bitwise-filter input 0 %) [0 1])
     (map #(Long/parseLong % 2))
     (apply *))
;; => 1007985
```

