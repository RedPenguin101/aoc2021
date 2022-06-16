#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int most_frequent(const char* input, int pos, int row_count, int row_len) {
  int count1s = 0;
  int valid_rows = 0;

  for (int row = 0; row < row_count; row++) {
    if (input[(row * row_len) + pos] == '1')
      count1s += 1;
    if (input[row * row_len] != 0)
      valid_rows += 1;
  }

  if (2 * count1s > valid_rows)
    return '1';
  if (2 * count1s < valid_rows)
    return '0';
  return '=';
}

int eliminate_input(char* input, int pos, char c, int row_count, int row_len) {
  int valid_rows = 0;
  for (int row = 0; row < row_count; row++) {
    if (input[(row * row_len) + pos] == c) {
      for (int i = row * row_len; i < (row + 1) * row_len; i++) {
        input[i] = 0;
      }
    }
    if (input[(row * row_len) + pos] != 0)
      valid_rows += 1;
  }
  return valid_rows;
}

int first_valid_row(char* input) {
  int idx = 0;
  while (!input[idx])
    idx += 1;

  return (int)strtol(&input[idx], NULL, 2);
}

int ogr_eliminate(char mf) {
  if (mf == '1' || mf == '=')
    return '0';
  return '1';
}

int csr_eliminate(char mf) {
  if (mf == '1' || mf == '=')
    return '1';
  return '0';
}

int solve(char* input, int input_len, int rows, char c) {
  int remaining_rows = rows;
  char eliminate;

  for (int pos = 0; pos < input_len - 1; pos++) {
    char mf = most_frequent(input, pos, rows, input_len);

    eliminate = (c == 'c') ? csr_eliminate(mf) : ogr_eliminate(mf);

    remaining_rows = eliminate_input(input, pos, eliminate, rows, input_len);

    if (remaining_rows == 1)
      return first_valid_row(input);
  }

  return 0;
}

int main() {
  int row = 0;
  int input_len = -1;
  char line[50] = {0};
  int counts[50] = {0};
  char most_common[50] = {0};

  char* input;
  char* input_copy;

  while (fgets(line, 20, stdin)) {
    if (row == 0) {
      input_len = strlen(line);
      input = calloc(1000, sizeof(char) * input_len);
      input_copy = calloc(1000, sizeof(char) * input_len);
    }

    line[input_len - 1] = '\0';
    strcpy(&input[row * input_len], line);
    strcpy(&input_copy[row * input_len], line);
    row += 1;

    // PART 1
    for (int i = 0; i < input_len - 1; i++) {
      if (line[i] == '1')
        counts[i] += 1;
    }
  }

  for (int i = 0; i < input_len - 1; i++) {
    if (counts[i] > (row / 2))
      most_common[i] = '1';
    else
      most_common[i] = '0';
  }

  int x = strtol(most_common, NULL, 2);
  int mask = (int)pow(2, input_len - 1) - 1;
  printf("Part1: %d\n", x * (~x & mask));

  // PART 2
  int ogr_val = solve(input, input_len, row, 'o');
  int csr_val = solve(input_copy, input_len, row, 'c');

  printf("Part2: %d\n", ogr_val * csr_val);

  return 0;
}
