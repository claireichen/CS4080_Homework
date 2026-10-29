#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "hash_benchmark.h"
#include "object.h"
#include "table.h"
#include "value.h"

static double nowSeconds(void) {
  return (double)clock() / CLOCKS_PER_SEC;
}

static void printResult(const char* name, int operations,
                        double elapsed) {
  double millionsPerSecond =
      elapsed > 0.0 ? operations / elapsed / 1000000.0 : 0.0;
  printf("%-28s %8.3f ms  %8.2f M ops/s\n",
         name, elapsed * 1000.0, millionsPerSecond);
}

static void benchmarkNumbers(int count, double* checksum) {
  Table table;
  initTable(&table);

  double start = nowSeconds();
  for (int i = 0; i < count; i++) {
    tableSet(&table, NUMBER_VAL(i), NUMBER_VAL(i * 2.0));
  }
  printResult("number insert + growth", count, nowSeconds() - start);

  Value result;
  start = nowSeconds();
  for (int i = 0; i < count; i++) {
    if (tableGet(&table, NUMBER_VAL(i), &result)) {
      *checksum += AS_NUMBER(result);
    }
  }
  printResult("number successful lookup", count, nowSeconds() - start);

  start = nowSeconds();
  for (int i = 0; i < count; i++) {
    tableGet(&table, NUMBER_VAL(count + i), &result);
  }
  printResult("number unsuccessful lookup", count, nowSeconds() - start);

  start = nowSeconds();
  for (int i = 0; i < count; i += 2) {
    tableDelete(&table, NUMBER_VAL(i));
  }
  printResult("delete half of keys", (count + 1) / 2,
              nowSeconds() - start);

  start = nowSeconds();
  for (int i = 0; i < count; i += 2) {
    tableGet(&table, NUMBER_VAL(i), &result);
  }
  printResult("lookup through tombstones", (count + 1) / 2,
              nowSeconds() - start);

  start = nowSeconds();
  for (int i = 0; i < count; i += 2) {
    tableSet(&table, NUMBER_VAL(i), NUMBER_VAL(i * 2.0));
  }
  printResult("reinsert deleted keys", (count + 1) / 2,
              nowSeconds() - start);

  freeTable(&table);
}

static uint64_t nextRandom(uint64_t* state) {
  *state ^= *state << 13;
  *state ^= *state >> 7;
  *state ^= *state << 17;
  return *state;
}

static void benchmarkRandomNumbers(int count, double* checksum) {
  Value* keys = (Value*)malloc(sizeof(Value) * count);
  if (keys == NULL) exit(1);

  uint64_t state = UINT64_C(0x40802026);
  for (int i = 0; i < count; i++) {
    uint64_t bits = nextRandom(&state) & UINT64_C(0x1fffffffffffff);
    keys[i] = NUMBER_VAL((double)bits);
  }

  Table table;
  initTable(&table);

  double start = nowSeconds();
  for (int i = 0; i < count; i++) {
    tableSet(&table, keys[i], NUMBER_VAL(i));
  }
  printResult("random number insert", count, nowSeconds() - start);

  Value result;
  start = nowSeconds();
  for (int i = 0; i < count; i++) {
    if (tableGet(&table, keys[i], &result)) {
      *checksum += AS_NUMBER(result);
    }
  }
  printResult("random number lookup", count, nowSeconds() - start);

  freeTable(&table);
  free(keys);
}

static void benchmarkStrings(int count, double* checksum) {
  Value* keys = (Value*)malloc(sizeof(Value) * count);
  if (keys == NULL) exit(1);

  char buffer[32];
  for (int i = 0; i < count; i++) {
    int length = snprintf(buffer, sizeof(buffer), "shared-prefix-%d", i);
    keys[i] = OBJ_VAL(copyString(buffer, length));
  }

  Table table;
  initTable(&table);

  double start = nowSeconds();
  for (int i = 0; i < count; i++) {
    tableSet(&table, keys[i], NUMBER_VAL(i));
  }
  printResult("string insert + growth", count, nowSeconds() - start);

  Value result;
  start = nowSeconds();
  for (int i = 0; i < count; i++) {
    if (tableGet(&table, keys[i], &result)) {
      *checksum += AS_NUMBER(result);
    }
  }
  printResult("string successful lookup", count, nowSeconds() - start);

  freeTable(&table);
  free(keys);
}

void runHashBenchmarks(int count) {
  double checksum = 0.0;

  printf("Hash-table benchmark (%d keys)\n", count);
  benchmarkNumbers(count, &checksum);
  benchmarkRandomNumbers(count, &checksum);
  benchmarkStrings(count, &checksum);
  printf("checksum: %.0f\n", checksum);
}
