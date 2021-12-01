# Notes on Zig solutions

## Day 1

Decided to try to use stdio for everything.

Nothing super interesting in this day. A couple of snips:

``` zig
const stdin = std.io.getStdIn();
const stdout = std.io.getStdOut();

while (try stdin.reader().readUntilDelimiterOrEof(&num_buff, '\n')) |num_str| {}

try stdout.writer().print("Part 1: {d}\n", .{count_p1});
```
