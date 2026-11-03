#include <stdio.h>

#include "continue_test.h"
#include "vm.h"

bool runContinueTest(void) {
  const char* validSource =
      "var i = 0;\n"
      "while (i < 5) {\n"
      "  i = i + 1;\n"
      "  if (i == 2) continue;\n"
      "  print i;\n"
      "}\n"
      "for (var j = 0; j < 5; j = j + 1) {\n"
      "  var scoped = j;\n"
      "  if (j < 3) continue;\n"
      "  print scoped;\n"
      "}\n"
      "var outer = 0;\n"
      "while (outer < 2) {\n"
      "  outer = outer + 1;\n"
      "  var bodyLocal = outer;\n"
      "  { var nestedLocal = bodyLocal; continue; }\n"
      "}\n"
      "print outer;\n";

  printf("continue output:\n");
  bool validPassed = interpret(validSource) == INTERPRET_OK;

  printf("outside-loop continue rejected: ");
  bool invalidRejected =
      interpret("continue;") == INTERPRET_COMPILE_ERROR;
  printf("%s\n", invalidRejected ? "yes" : "no");

  return validPassed && invalidRejected;
}
