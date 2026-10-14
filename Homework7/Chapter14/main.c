#include <stdio.h>

#include "chunk.h"
#include "debug.h"

int main(void) {
  Chunk chunk;
  initChunk(&chunk);

  int constant = addConstant(&chunk, 1.2);

  // Two bytes on line 123 should produce only one LineStart.
  writeChunk(&chunk, OP_CONSTANT, 123);
  writeChunk(&chunk, (uint8_t)constant, 123);
  writeChunk(&chunk, OP_RETURN, 124);

  disassembleChunk(&chunk, "test chunk");

  printf("\ncode bytes: %d\n", chunk.count);
  printf("line runs: %d\n", chunk.lineCount);
  for (int i = 0; i < chunk.count; i++) {
    printf("offset %d -> line %d\n", i, getLine(&chunk, i));
  }

  freeChunk(&chunk);
  return 0;
}
