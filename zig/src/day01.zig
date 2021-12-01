const std = @import("std");

const stdin = std.io.getStdIn();
const stdout = std.io.getStdOut();

pub fn main() !void {
  var num_buff: [100]u8 = undefined;
  var previous_num: usize = 0;

  var count_p1: usize = 0;

  var i: usize = 0;
  var count_p2: usize = 0;
  var sw: [4]usize = .{0,0,0,0};

  while (try stdin.reader().readUntilDelimiterOrEof(&num_buff, '\n')) |num_str| {
      const this_num = try std.fmt.parseInt(usize, num_str, 10);
      
      sw[1] += this_num;
      sw[2] += this_num;
      sw[3] += this_num;

      if ((i > 2) and (sw[0] < sw[1])) count_p2 += 1;
      sw[0] = sw[1];
      sw[1] = sw[2];
      sw[2] = sw[3];
      sw[3] = 0;
      i += 1;

      // Part 1 (has to be below because of that continue, bleh)
      if (previous_num == 0) { previous_num = this_num; continue; }
      if (this_num > previous_num) count_p1 += 1;
      previous_num = this_num;
  }

  try stdout.writer().print("Part 1: {d}\n", .{count_p1});
  try stdout.writer().print("Part 2: {d}\n", .{count_p2});
}
