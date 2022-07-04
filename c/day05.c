#include <math.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "hashset.h"

// Client
void print_line(int* line) {
  printf("x1=%d, y1=%d -> x2=%d, y2=%d\n", line[0], line[1], line[2], line[3]);
}
bool is_horizontal(int* line) {
  return line[1] == line[3];
}

bool is_vertical(int* line) {
  return line[0] == line[2];
}

int main() {
  int line_count = 0;
  char line_buff[25] = {0};

  int* lines = (int*)calloc(500, sizeof(int[4]));

  while (fgets(line_buff, 25, stdin)) {
    int x1, x2, y1, y2;
    sscanf(line_buff, "%d,%d -> %d,%d", &x1, &y1, &x2, &y2);
    lines[(line_count * 4) + 0] = x1;
    lines[(line_count * 4) + 1] = y1;
    lines[(line_count * 4) + 2] = x2;
    lines[(line_count * 4) + 3] = y2;
    line_count += 1;
  }

  hashset_t set = hashset_create();

  for (int lineA = 0; lineA < line_count; lineA++) {
    int* lineA_p = &lines[lineA * 4];
    if (is_horizontal(lineA_p) || is_vertical(lineA_p)) {
      for (int lineB = lineA + 1; lineB < line_count; lineB++) {
        int* lineB_p = &lines[lineB * 4];
        if (is_horizontal(lineB_p) || is_vertical(lineB_p)) {
          printf("Both H or V, A=%d, B=%d\n", lineA, lineB);
          print_line(lineA_p);
          print_line(lineB_p);
        }
      }
    }
  }

  // PART1
  printf("Part1: %d\n", 0);

  // PART 2
  printf("Part2: %d\n", 0);

  return 0;
}
