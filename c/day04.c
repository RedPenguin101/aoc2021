#include <math.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
  int* ptr;
  int len;
} intslice;

void print_slice(intslice x) {
  for (int i = 0; i < x.len; i++) {
    printf("%d", x.ptr[i]);
    if (i != x.len - 1)
      printf(", ");
  }
  printf("\n");
}

bool charin(char c, const char* str) {
  for (int i = 0; str[i]; i++) {
    if (str[i] == c)
      return true;
  }
  return false;
}

intslice parse_ints(char* str_buff, int max_len, const char* separators) {
  int* array = calloc(max_len, sizeof(int));
  int array_len = 0;

  char digits[3] = {0};
  int dig_pos = 0;

  for (int i = 0; str_buff[i]; i++) {
    if (charin(str_buff[i], separators) && dig_pos > 0) {
      int num = atoi(digits);
      array[array_len] = num;
      array_len += 1;

      digits[0] = 0;
      digits[1] = 0;
      digits[2] = 0;
      dig_pos = 0;
    } else {
      digits[dig_pos] = str_buff[i];
      dig_pos += 1;
    }
  }

  int num = atoi(digits);
  array[array_len] = num;
  array_len += 1;
  intslice x = {array, array_len};

  return x;
}

bool is_row_complete(int* board, int row_num) {
  bool complete = true;
  for (int col = 0; col < 5; col++) {
    if (board[(row_num * 5) + col] != -1)
      complete = false;
  }
  return complete;
}

bool is_col_complete(int* board, int col_num) {
  bool complete = true;
  for (int row = 0; row < 5; row++) {
    if (board[(row * 5) + col_num] != -1)
      complete = false;
  }
  return complete;
}

bool mark_and_check(int* board, int num) {
  for (int row = 0; row < 5; row++) {
    for (int col = 0; col < 5; col++) {
      int board_pos = (row * 5) + col;
      if (board[board_pos] == num) {
        board[board_pos] = -1;
        return is_col_complete(board, col) || is_row_complete(board, row);
      }
    }
  }
  return false;
}

int time_to_win(int* board, intslice* draws) {
  bool complete = false;
  for (int draw = 0; draw < draws->len; draw++) {
    complete = mark_and_check(board, draws->ptr[draw]);
    if (complete)
      return draw += 1;
  }
  return -1;
}

int score_board(int* board) {
  int score = 0;
  for (int row = 0; row < 5; row++) {
    for (int col = 0; col < 5; col++) {
      int board_pos = (row * 5) + col;
      if (board[board_pos] != -1) {
        score += board[board_pos];
      }
    }
  }
  return score;
}

int main() {
  char first_line[1000] = {0};
  intslice draws;

  fgets(first_line, 1000, stdin);
  draws = parse_ints(first_line, 100, ",");

  int* boards = (int*)calloc(1000, sizeof(int[5][5]));
  int board_num = 0;

  char line[100] = {0};
  while (fgets(line, 100, stdin)) {
    for (int row = 0; row < 5; row++) {
      fgets(line, 100, stdin);
      int a, b, c, d, e;
      sscanf(line, "%d %d %d %d %d", &a, &b, &c, &d, &e);
      int x = (board_num * 25) + (row * 5);
      boards[x + 0] = a;
      boards[x + 1] = b;
      boards[x + 2] = c;
      boards[x + 3] = d;
      boards[x + 4] = e;
    }
    board_num += 1;
  }

  int* ttw = (int*)calloc(board_num, sizeof(int));

  for (int board = 0; board < board_num; board++) {
    int ttwb = time_to_win(&boards[board * 25], &draws);
    ttw[board] = ttwb;
  }

  int min = 25;
  int win_board = 0;
  int max = 0;
  int lose_board = 0;
  for (int b = 0; b < board_num; b++) {
    if (ttw[b] < min) {
      min = ttw[b];
      win_board = b;
    }
    if (ttw[b] > max) {
      max = ttw[b];
      lose_board = b;
    }
  }

  // PART1
  printf("Part1: %d\n",
         score_board(&boards[25 * win_board]) * draws.ptr[min - 1]);
  // PART 2
  printf("Part2: %d\n",
         score_board(&boards[25 * lose_board]) * draws.ptr[max - 1]);

  return 0;
}
