const std = @import("std");

const stdin = std.io.getStdIn();
const stdout = std.io.getStdOut();

pub fn main() !void {
    var num_str: [1000]u8 = undefined;
    const line = try stdin.reader().readUntilDelimiterOrEof(&num_str, '\n');
    //var line_it = std.mem.tokenize(line, ",");

    var board_str: [1000]u8 = undefined;
    var x = try stdin.reader().readAll(&board_str);
    var boards_it = std.mem.split(u8, x, "\n\n");

    for (boards_it) |str| {
        var num_it = std.mem.tokenize(str, " \n");

        try stdout.writer().print("Board: {s}", .{num_it});
    }

    try stdout.writer().print("Out: {s}", .{line});
}
