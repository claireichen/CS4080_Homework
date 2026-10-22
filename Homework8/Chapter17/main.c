#include <stdio.h>

#include "chunk.h"
#include "compiler.h"
#include "debug.h"

int main(void) {
  Chunk chunk;
  initChunk(&chunk);

  const char* source = "1 ? 2 : 3 ? 4 : 5";
  printf("source: %s\n", source);

  bool success = compile(source, &chunk);
  if (success) {
    printf("ternary parser: accepted\n");
    disassembleChunk(&chunk, "parsed operands (no conditional bytecode)");
  }

  freeChunk(&chunk);
  return success ? 0 : 1;
}
