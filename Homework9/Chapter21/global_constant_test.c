#include <stdio.h>
#include <string.h>

#include "chunk.h"
#include "compiler.h"
#include "global_constant_test.h"
#include "object.h"
#include "value.h"
#include "vm.h"

bool runGlobalConstantTest(void) {
  const char* source =
      "var repeated = 1;\n"
      "print repeated;\n"
      "repeated = repeated + repeated;\n"
      "print repeated;\n";

  Chunk chunk;
  initChunk(&chunk);
  bool compiled = compile(source, &chunk);

  int identifierEntries = 0;
  for (int i = 0; i < chunk.constants.count; i++) {
    Value constant = chunk.constants.values[i];
    if (!IS_STRING(constant)) continue;

    ObjString* string = AS_STRING(constant);
    if (string->length == 8 &&
        memcmp(string->chars, "repeated", 8) == 0) {
      identifierEntries++;
    }
  }

  printf("identifier entries for repeated: %d\n", identifierEntries);
  printf("total constants: %d\n", chunk.constants.count);
  freeChunk(&chunk);

  if (!compiled || identifierEntries != 1) return false;

  printf("runtime output:\n");
  return interpret(source) == INTERPRET_OK;
}
