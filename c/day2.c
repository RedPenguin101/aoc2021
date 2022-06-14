#include <stdio.h>
#include <stdlib.h>

int main() {
  FILE* file = fopen("day02input.txt", "r");
  if (file == 0) {
    printf("could not open file.\n");
    return 1;
  }

  char line[20] = {0};
  // pos[0] is hor. pos[1] is depth for part 1, aim for part 2
  // pos[2] is depth for part 2.
  int pos[3] = {0, 0, 0};

  while (fgets(line, 20, file)) {
    int x = -1;
    char y[7] = {0};
    sscanf(line, "%s %d", y, &x);

    if (y[0] == 'u') {
      pos[1] -= x;
    } else if (y[0] == 'd') {
      pos[1] += x;
    } else if (y[0] == 'f') {
      pos[0] += x;
      pos[2] += pos[1] * x;
    }
  }

  printf("Part1: %d\n", pos[0] * pos[1]);
  printf("Part2: %d\n", pos[0] * pos[2]);

  fclose(file);
  return 0;
}
