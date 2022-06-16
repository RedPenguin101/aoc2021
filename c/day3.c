#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int is_most_frequent(const char* input,
                     int pos,
                     int input_rows,
                     int row_len,
                     char character) {
  int count = 0;
  int valid_rows = 0;
  for (int i = 0; i < input_rows; i++) {
    // printf("IMF String testing row %d: %s\n", i, &input[(i * row_len)]);
    if (input[(i * row_len) + pos] == character)
      count += 1;
    if (input[i * row_len] != 0) {
      //  printf("validity: %d\n", input[i * row_len]);
      valid_rows += 1;
    }
  }
  // printf("IMF CALC for %c in pos %d, IRs=%d: count %d, vrs %d\n", character,
  //       pos, input_rows, count, valid_rows);
  if (2 * count > valid_rows)
    return 1;
  if (2 * count == valid_rows)
    return 0;
  return -1;
}

void wipe_input(char* input, int row, int row_len) {
  for (int i = row * row_len; i < (row + 1) * row_len; i++) {
    input[i] = 0;
  }
}

char* ptr_first_valid_row(char* input, int row_len, int rows) {
  for (int i = 0; i < (rows * row_len); i += row_len) {
    if (input[i] != 0) {
      return &input[i];
    }
  }
  return 0;
}

int ogr(char* input, int input_str_len, int rows, char search_char) {
  int remaining_rows = rows;
  char eliminate;

  for (int i = 0; i < input_str_len - 1; i++) {
    if (remaining_rows == 1) {
      char* results_pointer = ptr_first_valid_row(input, input_str_len, rows);
      if (results_pointer)
        return (int)strtol(results_pointer, NULL, 2);
      else
        return 0;
    }

    int imf = is_most_frequent(input, i, rows, input_str_len, search_char);
    if (imf >= 0) {
      eliminate = '0';
    } else {
      eliminate = '1';
    }

    for (int j = 0; j < rows; j++) {
      if (input[(j * input_str_len) + i] == eliminate) {
        remaining_rows -= 1;
        wipe_input(input, j, input_str_len);
      }
    }
  }

  char* results_pointer = ptr_first_valid_row(input, input_str_len, rows);

  if (results_pointer)
    return (int)strtol(results_pointer, NULL, 2);
  else
    return 0;
}

int csr(char* input, int input_str_len, int rows, char search_char) {
  int remaining_rows = rows;
  char eliminate;

  for (int i = 0; i < input_str_len - 1; i++) {
    // Circuit break if there's one value remaining
    if (remaining_rows == 1) {
      char* results_pointer = ptr_first_valid_row(input, input_str_len, rows);
      if (results_pointer)
        return (int)strtol(results_pointer, NULL, 2);
      else
        return 0;
    }

    int imf = is_most_frequent(input, i, rows, input_str_len, search_char);
    // printf("IMF: %d\n", imf);
    if (imf <= 0) {
      eliminate = '1';
    } else {
      eliminate = '0';
    }

    for (int j = 0; j < rows; j++) {
      if (input[(j * input_str_len) + i] == eliminate) {
        remaining_rows -= 1;
        wipe_input(input, j, input_str_len);
      }
    }

    // printf("Input Array:\n");
    // for (int i = 0; i < rows; i++) {
    //  printf("Row %d, pos %d: %s\n", i, i * input_str_len,
    //         &input[i * input_str_len]);
    //}
  }

  return 0;
}

int main() {
  int row = 0;
  int input_row_len = -1;
  int input_str_len = -1;
  char line[50] = {0};
  int counts[50] = {0};
  char most_common[50] = {0};

  char* input;
  char* input_copy;

  while (fgets(line, 20, stdin)) {
    if (row == 0) {
      input_str_len = strlen(line);
      input_row_len = input_str_len - 1;
      input = calloc(1000, sizeof(char) * input_str_len);
      input_copy = calloc(1000, sizeof(char) * input_str_len);
    }

    line[input_row_len] = '\0';
    strcpy(&input[row * input_str_len], line);
    strcpy(&input_copy[row * input_str_len], line);
    row += 1;

    for (int i = 0; i < input_row_len; i++) {
      if (line[i] == '1')
        counts[i] += 1;
    }
  }

  for (int i = 0; i < input_row_len; i++) {
    if (counts[i] > (row / 2))
      most_common[i] = '1';
    else
      most_common[i] = '0';
  }

  int x = strtol(most_common, NULL, 2);
  int mask = (int)pow(2, input_row_len) - 1;
  printf("Part1: gamma=%d, eps=%d, result=%d\n", x, (~x & mask),
         x * (~x & mask));

  // PART 2

  int ogr_val = ogr(input, input_str_len, row, '1');
  int csr_val = csr(input_copy, input_str_len, row, '0');
  printf("Part2: ogr=%d, csr=%d, result=%d\n", ogr_val, csr_val,
         ogr_val * csr_val);

  return 0;
}

