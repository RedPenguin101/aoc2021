# Zig Cheat Sheet

## Syntax

`.?` is shorthand for `orelse unreachable`. Used when a call returns an option type, and you want an `null` to throw. 

```zig
const dir = line_it.next().?;
```

`try x` is a shorthand for `x catch |err| return err`

Struct with methods.

```zig
const Pair = struct {
    lhs: u8,
    rhs: u8,

    pub fn equals(lhs: Pair, rhs: Pair) bool {
        return lhs.lhs == rhs.lhs and lhs.rhs == rhs.rhs;
    }

    pub fn hash(self: Pair) u32 {
        return @intCast(u32, self.lhs) + @intCast(u32, self.rhs) * 100;
    }
};

const pair = Pair { .lhs = pair_string[0], .rhs = pair_string[1], };
```

Anonymous structs:

```zig
fn getCoords(index: usize) struct { x: usize, y: usize } {
    return .{
        .x = index % LINE_LENGTH,
        .y = index / LINE_LENGTH,
    };
}
```

Switch statement

```zig
switch (dir[0]) {
    'f' => x += num,
    'u' => y -= num,
    'd' => y += num,
    else => @panic("couldn't match direction"),
}
```

## Loops

While with continue expression:

```zig
while (i <= 10) : (i += 1) {
    sum += i;
}
```

For with index capture

```zig
for (string) |character, index| {
}
```

For with pointer

```zig
for (grid) |*value, i| {
    value.* += 1; // will modify the elements of grid
    if (value.* > 9) {
        value.* = 100;
        try flash_indices.append(i);
    }
}
```

## Macros

```zig
@minimum(a, b);
@maximum(a, b);
@intCast(u4, char);
@panic("message");
@as(u32, 1234);
@sizeOf(usize);
@embedFile("./resouces/day05input.txt");
```

## Split vs. Tokenize


## IO

```zig
var file = try std.fs.cwd().openFile("./resources/day05input.txt", .{});
defer file.close();

const stdin = std.io.getStdIn();
const stdout = std.io.getStdOut();
```

### Readers
Reading from stdin

```zig
while (try stdin.reader().readUntilDelimiterOrEof(&num_buff, '\n')) |num_str| {}
var x = try stdin.reader().readAll(&board_str);

const x1_string = try file.reader().readUntilDelimiter(buffer[0..], ',');
const char = try file.reader().readByte();
file.reader().skipUntilDelimiterOrEof('\n');
```

### Writers

Write to stdout

```zig
try stdout.writer().print("Day 1 Part 1: {}\n", .{count_p1});
```

## String formatting

```zig
std.debug.print("vent counts: {}\n", .{vent_counts.len});
```

## Arrays

Initialize an array of zeros

```zig
var vent_counts = [_]i32{0} ** (1000 * 500);
```

Slice array with `array[0..3]`. The slice type is a pointer and a size. `array[0..]` is a slice to the end.

## Parsing

```zig
try std.fmt.parseInt(usize, num_str, 2);
```

## Numbers

```zig
var least_common: u64 = std.math.maxInt(u64);
```

## Pointers

* Type syntax `*T` for pointee type `T`.
* Compute pointer with `&variable`
* Deference with `variable.*`
* No `NULL` (0) pointer from C.

```zig
fn increment(num: *u8) void {
    num.* += 1;
}

fn main() !void {
    var x: u8 = 1;
    increment(&x);
}
```

* const pointer (`*const T`) can't be used to modify referenced data.
* A computed pointer to a const will yield a const pointer.

## Dynamic memory and allocators

```zig
test "GPA" {
    var gpa = std.heap.GeneralPurposeAllocator(.{}){};
    const allocator = gpa.allocator();
    defer {
        const leaked = gpa.deinit();
        if (leaked) expect(false) catch @panic("TEST FAIL"); //fail test; can't try in defer as defer is executed after we return
    }
    
    const bytes = try allocator.alloc(u8, 100);
    defer allocator.free(bytes);
}
```

```zig
test "allocation" {
    const allocator = std.heap.page_allocator;

    const memory = try allocator.alloc(u8, 100);
    defer allocator.free(memory);
    // defer and free pattern

    try expect(@TypeOf(memory) == []u8);
    try expect(memory.len == 100);
}
```

Fixed buffer allocator is stack allocator.

```zig
test "fixed buffer allocator" {
    var buffer: [1000]u8 = undefined;
    var fba = std.heap.FixedBufferAllocator.init(&buffer);
    const allocator = fba.allocator();

    const memory = try allocator.alloc(u8, 100);
    defer allocator.free(memory);

    try expect(memory.len == 100);
    try expect(@TypeOf(memory) == []u8);
}
```

## enums

```zig
const CaveType = enum {
    Start,
    End,
    Small,
    Large,
};

fn getCaveType(cave: []const u8) CaveType {
    if (equals(cave, "start")) return .Start;
    if (equals(cave, "end")) return .End;
    if (cave[0] >= 'A' and cave[0] <= 'Z') return .Large;
    if (cave[0] >= 'a' and cave[0] <= 'z') return .Small;
    unreachable;
}
```

## tests

```zig
test "test-input-1" {
    const result = try execute(test_input_1);
    const expected: u32 = 36;
    try std.testing.expectEqual(expected, result);
}
```

## Hashmaps

```zig
var new_pairs = std.AutoHashMap(u32, u64).init(alloc.allocator());
defer new_pairs.deinit();
```

methods

* `put`
* `getOrPut`
* `clearRetainingCapacity`

## Build

`zig build-exe file.zig`