#include <stdio.h>
#include <stdlib.h>

int main()
{
  FILE *file = fopen("day01input.txt", "r");
  if (file == 0)
  {
    printf("could not open file.\n");
    return 1;
  }

  char line[8];
  int values[2000];
  int idx = 0;
  while (fgets(line, 8, file))
  {
    values[idx] = atoi(line);
    idx++;
  }

  int part1 = 0;

  for (int i = 1; i < 2000; i++)
  {
    if (values[i] > values[i - 1])
      part1++;
  }

  printf("Part1: %i\n", part1);

  int part2 = 0;

  for (int i = 3; i < 2000; i++)
  {
    if (values[i] > values[i - 3])
      part2++;
  }
  printf("Part2: %i\n", part2);

  fclose(file);
  return 0;
}