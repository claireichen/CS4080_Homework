#include <stdio.h>

#include "chunk.h"
#include "vm.h"

int main(void) {
  initVM();

  Chunk chunk;
  initChunk(&chunk);

  // Push 20 operands before performing any additions. A fixed eight-slot
  // starting allocation must grow to hold this expression.
  for (int value = 1; value <= 20; value++) {
    writeConstant(&chunk, (Value)value, 123);
  }

  for (int operation = 1; operation < 20; operation++) {
    writeChunk(&chunk, OP_ADD, 124);
  }

  writeChunk(&chunk, OP_RETURN, 125);

  InterpretResult result = interpret(&chunk);
  if (result == INTERPRET_OK) {
    printf("maximum test depth: 20\n");
    printf("final stack capacity: %d\n", vm.stackCapacity);
  }

  freeChunk(&chunk);
  freeVM();
  return result == INTERPRET_OK ? 0 : 1;
}
