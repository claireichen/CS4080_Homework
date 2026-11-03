#include <stdio.h>
#include <stdlib.h>

#include "chunk.h"
#include "compiler.h"
#include "long_local_test.h"
#include "vm.h"

bool runLongLocalTest(void) {
  const int localCount = 300;
  const size_t capacity = 16000;
  char* source = (char*)malloc(capacity);
  if (source == NULL) return false;

  size_t length = (size_t)snprintf(source, capacity, "{\n");
  for (int i = 0; i < localCount; i++) {
    length += (size_t)snprintf(source + length, capacity - length,
                               "var local%d;\n", i);
  }
  length += (size_t)snprintf(
      source + length, capacity - length,
      "local0 = true;\n"
      "local255 = false;\n"
      "local256 = true;\n"
      "print local0;\n"
      "print local255;\n"
      "print local256;\n"
      "print local299;\n"
      "local299 = local256;\n"
      "print local299;\n"
      "}\n");

  if (length >= capacity) {
    free(source);
    return false;
  }

  Chunk chunk;
  initChunk(&chunk);
  bool compiled = compile(source, &chunk);
  bool sawLongGet = false;
  bool sawLongSet = false;
  for (int i = 0; i < chunk.count; i++) {
    if (chunk.code[i] == OP_GET_LOCAL_LONG) sawLongGet = true;
    if (chunk.code[i] == OP_SET_LOCAL_LONG) sawLongSet = true;
  }
  freeChunk(&chunk);

  printf("locals declared: %d\n", localCount);
  printf("long local read emitted: %s\n", sawLongGet ? "yes" : "no");
  printf("long local write emitted: %s\n", sawLongSet ? "yes" : "no");
  printf("runtime output:\n");
  bool ran = interpret(source) == INTERPRET_OK;
  free(source);

  return compiled && sawLongGet && sawLongSet && ran;
}
