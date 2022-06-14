# Day 19: Beacon Scanner

[Link to problem](https://adventofcode.com/2021/day/19)

## Problem description
We are to build a 3d map of a trench using the output of beacons and scanners. Scanners detect all beacons within 1000 units, and report their position relative to its own.

The scanners have some limitations:

1. Scanners can't detect other scanners
2. Scanners don't know their own position
3. Scanners don't know their own rotation (90 degree increments in x, y and z directions)

Given the reading of the scanners, how do you know which beacons detected by each are the _same_ beacons?

Using the readings of the different scanners, and deducing when the scan-zones of the scanners overlap, you can infer a map of the area. The threshold for concluding that two scan-zones overlap is when 12 beacons from each scan-zone are in common.

Part 1: how many 'beacons' are there?

## Stripping down the problem
Clearly this is problem in three dimensional space. A scanner can have any position, and can have any one of 6 facings (the facings of a cube).

I feel like this is a problem that can be solved by linear algebra.

A given scanner $s_i$ is a 3d vector with its position in space, relative to some arbitrary origin. The scanner has a scan-zone $S_i = \{v_1, v_2, ... , v_n\}$, a set of vectors representing the scan results.

## Detecting intersections
$S_i$ (assuming 3 dimensions) has 6 _variants_ (together $R_i = \{S_i\}$), each a rotational transformation of $s_i$. Each variant is how the beacons would look if $s_i$ was rotated.

Next we have the translation problem. Assume that $S_i$ and $S_j$ have the same facing, so the 'correct' variant is the original one. How can we detect whether they 'overlap' without knowing the position of $s_i$ relative to $s_j$?

The simplified example in the problem is given like this:

```
--scanner 0--
...B.
B....
....B
S....

--scanner 1--
...B..
B....S
....B.

--map--
...B..
B....S
....B.
S.....
```

This is visually simple, but how to detect it programmatically? At least without huge computational cost.

A maybe naive way to do it is to calculate every beacons position relative to other beacons. So scanner 0 would have the following:

$$
S_{0,0} = \{(0, 2), (4, 1), (3,3)\} \\
S_{0,1} = \{(0, 0), (4, -1), (3,1)\} \\
S_{0,2} = \{(-3, 1), (0, 0), (-1,2)\} \\
S_{0,3} = \{(-3, -1), (1, -2), (0,0)\} \\
$$

Scanner 1 has:

$$
S_{1,0} = \{(-1, -1), (-5, 0), (-2,1)\} \\
S_{1,1} = \{(0, 0), (-4, 1), (-1,2)\} \\
S_{1,2} = \{(4, -1), (0, 0), (2,1)\} \\
S_{1,3} = \{(1, -2), (-3, -1), (0,0)\} \\
$$

We can see that $S_{1,3}=S_{0,3}$, so these overlap.

This introduces a size problem though. To tell if two $S$ have an overlap, we need to calculate the variants of each (12), and the translations of each ($n$, where $n$ is the number of variants in the set. Our input has about 25 elements per set, so we need to calculate 300 permutations. We then need to do a pairwise comparison of each of the elements, which is $150^2$, or 22,500

The number of comparisons we need to do is, I think, around 525[^1]. So a total of 11 million operations. This is not _totally_ unreasonable, so let's go with that for now.

[^1]: with 32 scanners, that's $\sum_{i=0}^{32}{i}=525$

So we have 2 problems:

* Given a set $S$, return the 6 rotational variant sets
* Given a set $S$, return the $n$ translational variant sets (where $n$ is the number of elements in the set)

The latter is easy: For every element in the set, return the set of all elements with pairwise deduction.

The former is trickier.

## Bleh
We'll say that two scanners $s_i$ and $s_j$ _elide_ when at least one variant in $\{S_i\}$ and $\{S_j\}$ have at least 12 elements in common.

So what do we need to solve this?

* A function `variants` which, given $S_i$, can return all variants $\{S_i\}$
* An algorithm which, given two variant sets can decide how many of them overlap, and maybe return a set of all vectors in the joined set.
* An algorithm which can tie is all together.

Starting with the overall algorithm:

0. The starting structure is a sequence of $S$. You'll also have a `visited` (empty set) and a `result-set` (empty set of vectors).
1. Start with an arbitrary $S$
2. Iterate through all other $S$, detecting where there are overlaps. (`overlaps?`)
3. For each overlap, find the `conjunction` of all variants and add them to the `result-set`.
4. Add the $S$ to `visited` and pick another $S$, go back to the start.
5. when all $S$ are visited, return the `result-set`, which will be the number of beacons.

The problem I see with this algorithm is keeping track of the rotation. Say you have three scanners, you pick one (S1), and find out that after rotation it overlaps with S2 (but not S3). Then you test S2 against S3 and _they_ overlap. But you can't conjoin S3 into the results set because the rotation of S2 to S3 might not be the same as S1 to S2.

I think the solution to this problem is that, as you're running through testing conjunction of S1 to SI, you are building the input to the next iteration by putting SI into the sequence as-if (if there is no overlap) but by _replacing_ SI with the variant that overlaps with S1 if it does. Then, when you pick the next SI to iterate, you just have to make sure it has the same facing as your original S1.

That's all 
