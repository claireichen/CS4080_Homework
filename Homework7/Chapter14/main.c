#include <stdio.h>

#include "chunk.h"
#include "debug.h"

int main(void) {
  Chunk chunk;
  initChunk(&chunk);

  // Index 0 fits in the original one-byte constant instruction.
  writeConstant(&chunk, 1.2, 123);

  // Fill indexes 1 through 255 without emitting instructions.
  for (int i = 1; i <= UINT8_MAX; i++) {
    addConstant(&chunk, (Value)i);
  }

  // This value receives index 256 and requires a 24-bit operand.
  writeConstant(&chunk, 999.0, 124);
  writeChunk(&chunk, OP_RETURN, 125);

  disassembleChunk(&chunk, "test chunk");

  printf("\nconstants: %d\n", chunk.constants.count);
  printf("code bytes: %d\n", chunk.count);
  printf("line runs: %d\n", chunk.lineCount);
  for (int i = 0; i < chunk.count; i++) {
    printf("offset %d -> line %d\n", i, getLine(&chunk, i));
  }

  freeChunk(&chunk);
  return 0;
}
