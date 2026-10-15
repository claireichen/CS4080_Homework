#include <stdio.h>
#include <time.h>

#include "chunk.h"
#include "vm.h"

int main(void) {
  const int negateCount = 100000000;

  initVM();

  Chunk chunk;
  initChunk(&chunk);

  writeConstant(&chunk, 1.0, 123);
  for (int i = 0; i < negateCount; i++) {
    writeChunk(&chunk, OP_NEGATE, 124);
  }
  writeChunk(&chunk, OP_RETURN, 125);

  clock_t start = clock();
  InterpretResult result = interpret(&chunk);
  clock_t end = clock();

  if (result == INTERPRET_OK) {
    double seconds = (double)(end - start) / CLOCKS_PER_SEC;
    printf("negations: %d\n", negateCount);
    printf("elapsed: %.6f seconds\n", seconds);
  }

  freeChunk(&chunk);
  freeVM();
  return result == INTERPRET_OK ? 0 : 1;
}
