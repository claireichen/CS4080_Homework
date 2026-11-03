#include <stdio.h>

#include "const_test.h"
#include "vm.h"

bool runConstTest(void) {
  const char* validSource =
      "const globalAnswer = 42;\n"
      "print globalAnswer;\n"
      "{\n"
      "  const localAnswer = 21;\n"
      "  print localAnswer;\n"
      "  var mutable = 1;\n"
      "  mutable = 2;\n"
      "  print mutable;\n"
      "}\n";
  const char* invalidLocal =
      "{ const fixedLocal = 1; fixedLocal = 2; }";
  const char* invalidGlobal =
      "const fixedGlobal = 1; fixedGlobal = 2;";

  printf("valid const and var output:\n");
  bool validPassed = interpret(validSource) == INTERPRET_OK;

  printf("local reassignment rejected: ");
  bool localRejected =
      interpret(invalidLocal) == INTERPRET_COMPILE_ERROR;
  printf("%s\n", localRejected ? "yes" : "no");

  printf("global reassignment rejected: ");
  bool globalRejected =
      interpret(invalidGlobal) == INTERPRET_COMPILE_ERROR;
  printf("%s\n", globalRejected ? "yes" : "no");

  return validPassed && localRejected && globalRejected;
}

