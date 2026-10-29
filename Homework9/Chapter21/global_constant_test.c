#include <stdio.h>
#include <string.h>

#include "chunk.h"
#include "compiler.h"
#include "global_constant_test.h"
#include "object.h"
#include "value.h"
#include "vm.h"

bool runGlobalSlotTest(void) {
  const char* source =
      "var repeated = 1;\n"
      "print repeated;\n"
      "repeated = repeated + repeated;\n"
      "print repeated;\n";

  Chunk chunk;
  initChunk(&chunk);
  bool compiled = compile(source, &chunk);

  int identifierConstants = 0;
  for (int i = 0; i < chunk.constants.count; i++) {
    Value constant = chunk.constants.values[i];
    if (!IS_STRING(constant)) continue;

    ObjString* string = AS_STRING(constant);
    if (string->length == 8 &&
        memcmp(string->chars, "repeated", 8) == 0) {
      identifierConstants++;
    }
  }

  printf("identifier constants for repeated: %d\n", identifierConstants);
  printf("total constants: %d\n", chunk.constants.count);
  printf("global slots: %d\n", vm.globalValues.count);

  bool hasOneUndefinedSlot =
      vm.globalValues.count == 1 && IS_UNDEFINED(vm.globalValues.values[0]);
  freeChunk(&chunk);

  if (!compiled || identifierConstants != 0 || !hasOneUndefinedSlot) {
    return false;
  }

  printf("runtime output:\n");
  if (interpret(source) != INTERPRET_OK) return false;

  Value finalValue = vm.globalValues.values[0];
  printf("final slot value: ");
  printValue(finalValue);
  printf("\n");
  return IS_NUMBER(finalValue) && AS_NUMBER(finalValue) == 2;
}
