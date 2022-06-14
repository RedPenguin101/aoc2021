const std = @import("std");

const stdin = std.io.getStdIn();
const stdout = std.io.getStdOut();

pub fn main() !void {
    var file = try std.fs.cwd().openFile("./resources/day05input.txt", .{});
    var vent_counts = [_]i32{0} ** (1000 * 500);

    var buffer: [10]u8 = undefined;
    var line_index: usize = 0;
    while (line_index < 500) : (line_index += 1) {
        const x1_string = try file.reader().readUntilDelimiter(buffer[0..], ',');
        const x1 = try std.fmt.parseInt(i32, x1_string, 10);
        const y1_string = try file.reader().readUntilDelimiter(buffer[0..], ' ');
        const y1 = try std.fmt.parseInt(i32, y1_string, 10);
        _ = try file.reader().readUntilDelimiter(buffer[0..], ' ');
        const x2_string = try file.reader().readUntilDelimiter(buffer[0..], ',');
        const x2 = try std.fmt.parseInt(i32, x2_string, 10);
        const y2_string = try file.reader().readUntilDelimiter(buffer[0..], '\n');
        const y2 = try std.fmt.parseInt(i32, y2_string, 10);

        _ = x1;
        _ = y1;
        _ = x2;
        _ = y2;
    }

    std.debug.print("vent counts: {d}\n", .{vent_counts.len});
}
