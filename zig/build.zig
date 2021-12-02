const Builder = @import("std").build.Builder;
const print = @import("std").debug.print;

pub fn build(b: *Builder) void {
    const exe1 = b.addExecutable("day01", "src/day01.zig");
    exe1.install();
    const exe2 = b.addExecutable("day02", "src/day02.zig");
    exe2.install();
}
