const std = @import("std");

const stdin = std.io.getStdIn();
const stdout = std.io.getStdOut();

pub fn main() !void {
    var line_buff: [13]u8 = undefined;
    var num_count: usize = 0;
    var ones: [12]usize = .{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    while (try stdin.reader().readUntilDelimiterOrEof(&line_buff, '\n')) |num_str| {
        count_ones(&ones, num_str);
        num_count += 1;
    }

    const cmp = num_count / 2;
    var most_bin: usize = 0;
    var least_bin: usize = 0;

    for (ones) |num_ones| {
        add_right(&most_bin, if (num_ones > cmp) 1 else 0);
        add_right(&least_bin, if (num_ones < cmp) 1 else 0);
    }

    try stdout.writer().print("Day 3 Part 1: {d}\n", .{most_bin * least_bin});
}

fn add_right(num: *usize, new_bit: u8) void {
    num.* = (num.* << 1) | new_bit;
}

fn count_ones(ones: *[12]usize, num_str: []u8) void {
    for (num_str) |char, idx| {
        if (char == '1') {
            ones.*[idx] += 1;
        }
    }
}
