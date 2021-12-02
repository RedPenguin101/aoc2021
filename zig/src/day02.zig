const std = @import("std");

const print = std.debug.print;
const stdin = std.io.getStdIn();
const stdout = std.io.getStdOut();

pub fn main() !void {
    var x: usize = 0;
    var y: usize = 0;

    var y2: usize = 0;
    var aim: usize = 0;

    var line_buf: [100]u8 = undefined;

    while (try stdin.reader().readUntilDelimiterOrEof(&line_buf, '\n')) |line| {
        var line_it = std.mem.tokenize(line, " ");
        const dir = line_it.next().?;
        const num_str = line_it.next().?;
        const num = try std.fmt.parseInt(usize, num_str, 10);

        // Part 1
        switch (dir[0]) {
            'f' => x += num,
            'u' => y -= num,
            'd' => y += num,
            else => @panic("couldn't match direction"),
        }

        // Part 2
        switch (dir[0]) {
            'f' => y2 += num * aim,
            'u' => aim -= num,
            'd' => aim += num,
            else => @panic("couldn't match direction"),
        }
    }

    try stdout.writer().print("{d}\n", .{x * y});
    try stdout.writer().print("{d}\n", .{x * y2});
}
