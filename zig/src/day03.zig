const std = @import("std");

const stdin = std.io.getStdIn();
const stdout = std.io.getStdOut();

fn read_input() ![1000][13]u8 {
    var line_buff: [13]u8 = undefined;
    var num_count: usize = 0;
    var input: [1000][13]u8 = undefined;

    while (try stdin.reader().readUntilDelimiterOrEof(&line_buff, '\n')) |_| {
        input[num_count] = line_buff;
        num_count += 1;
    }
    return input;
}

pub fn main() !void {
    const input = try read_input();
    try stdout.writer().print("Day 3 Part 1: {d}\n", .{most_common(input) * least_common(input)});
}

fn most_common(nums: [1000][13]u8) usize {
    var ones: [12]usize = .{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    var cmp: usize = 0;
    for (nums) |num_str| {
        count_ones(&ones, num_str);
        cmp += 1;
    }
    cmp = cmp / 2;
    var out: usize = 0;
    for (ones) |num_ones| {
        add_right(&out, if (num_ones > cmp) 1 else 0);
    }
    return out;
}

fn least_common(nums: [1000][13]u8) usize {
    var ones: [12]usize = .{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    var cmp: usize = 0;

    for (nums) |num_str| {
        count_ones(&ones, num_str);
        cmp += 1;
    }

    cmp = cmp / 2;
    var out: usize = 0;
    for (ones) |num_ones| {
        add_right(&out, if (num_ones < cmp) 1 else 0);
    }
    return out;
}

fn count_ones(ones: *[12]usize, num_str: [13]u8) void {
    for (num_str) |char, idx| {
        if (char == '1') {
            ones.*[idx] += 1;
        }
    }
}

fn add_right(num: *usize, new_bit: u8) void {
    num.* = (num.* << 1) | new_bit;
}
