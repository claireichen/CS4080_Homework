#include <stdio.h>

#include "native_library_test.h"
#include "vm.h"

bool runNativeLibraryTest(void) {
  const char* validSource =
      "print abs(-12.5);\n"
      "print min(7, 3);\n"
      "print max(7, 3);\n"
      "print pow(2, 8);\n"
      "print len(\"hello\");\n"
      "print type(nil);\n"
      "print type(true);\n"
      "print type(123);\n"
      "print type(\"lox\");\n"
      "print type(clock);\n";

  printf("native library output:\n");
  bool validPassed = interpret(validSource) == INTERPRET_OK;

  printf("len type error reported: ");
  bool lenRejected =
      interpret("len(123);") == INTERPRET_RUNTIME_ERROR;
  printf("%s\n", lenRejected ? "yes" : "no");

  printf("min type error reported: ");
  bool minRejected =
      interpret("min(\"seven\", 3);") == INTERPRET_RUNTIME_ERROR;
  printf("%s\n", minRejected ? "yes" : "no");

  return validPassed && lenRejected && minRejected;
}
