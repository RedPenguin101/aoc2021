# AOC2021 Day 1: Sonar Sweep

Given a list of integers, count the number of times the item in the list increases. For example:

```
199 (N/A - no previous measurement)
200 (increased)
208 (increased)
210 (increased)
200 (decreased)
207 (increased)
240 (increased)
269 (increased)
260 (decreased)
263 (increased)
```

Has 7 increases.

## Clojure

Here I can create a sliding window of 2 over the sequence, and compare each element of the window, filtering if the first element is smaller than the second.

``` clojure
(count (filter #(apply < %) (partition 2 1 input)))
;; => 1715
```

Part 2 has us the number of times the sum of measurements in a 3 sliding window increases from the previous sum. We can use the same strategy with an addition step to sum.

``` clojure
(->> input
     (partition 3 1)
     (map #(apply + %))
     (partition 2 1)
     (filter #(apply < %))
     count)
;; => 1739
```

## Zig

An imperative Zig solution will be a much longer.

For part 1, maintain a variable `previous_number` and test the new number vs the other.

For part 2, use the fact that, when comparing 'windows', the middle two terms cancel out, so you're effectively testing each `v[i]` with `v[i-3]`.

Here's an extract

``` zig
while (try stdin.reader().readUntilDelimiterOrEof(&num_buff, '\n')) |num_str| {
     const this_num = try std.fmt.parseInt(usize, num_str, 10);

     // part 1
     if ((i > 0) and (this_num > previous_num)) count_p1 += 1;
     previous_num = this_num;
      
     // part 2
     // In sliding window, 'middle' terms cancel, so only test required
     // is whether this number is greater than the one 3 indexes back
     if ((i > 2) and (sw[0] < this_num)) count_p2 += 1;
     sw[0] = sw[1];
     sw[1] = sw[2];
     sw[2] = this_num;
     i += 1;
}
```

the 'previous_num' is technically redundant, but combining with sw makes it harder to read.
