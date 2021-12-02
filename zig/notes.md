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

## Day 2

I didn't add it yesterday, but I used the zig build to make the binaries with the syntax `zig build-exe .\tiny-hello.zig -O ReleaseSafe`

Nothing super new today. The following, using tokenization to parse input, will probably be a very common pattern.

``` zig
var line_it = std.mem.tokenize(line, " ");
const dir = line_it.next().?;
const num_str = line_it.next().?;
const num = try std.fmt.parseInt(usize, num_str, 10);
```

And I think this is the first time I've use a switch in zig:


``` zig
switch (dir[0]) {
    'f' => x += num,
    'u' => y -= num,
    'd' => y += num,
    else => @panic("couldn't match direction"),
}
```

Not sure about that panic. Might be a better way to do that.

One thing I tried to to later in the day was add a build script. My intent was to do it with a loop, but I couldn't get it to work, both because (I think) re-using an executable is not intended, and because I can't figure out how to pad format a number.

So: How do you pad format a number? Maybe just string concat?
