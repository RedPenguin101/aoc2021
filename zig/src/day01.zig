const std = @import("std");

const stdin = std.io.getStdIn();
const stdout = std.io.getStdOut();

pub fn main() !void {
    var num_buff: [100]u8 = undefined;
    var previous_num: usize = 0;
    var count_p1: usize = 0;

    var i: usize = 0;
    var count_p2: usize = 0;
    var sw: [3]usize = .{ 0, 0, 0 };

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

    try stdout.writer().print("Day 1 Part 1: {d}\n", .{count_p1});
    try stdout.writer().print("Day 1 Part 2: {d}\n", .{count_p2});
}
