#include <stdio.h>

#include "native_arity_test.h"
#include "vm.h"

bool runNativeArityTest(void) {
  printf("correct arity output:\n");
  bool correctAccepted =
      interpret("print clock() >= 0;") == INTERPRET_OK;

  printf("incorrect arity rejected: ");
  bool incorrectRejected =
      interpret("clock(123);") == INTERPRET_RUNTIME_ERROR;
  printf("%s\n", incorrectRejected ? "yes" : "no");

  printf("recovery output:\n");
  bool recovered = interpret("print \"ok\";") == INTERPRET_OK;
  printf("VM recovered after error: %s\n",
         recovered ? "yes" : "no");

  return correctAccepted && incorrectRejected && recovered;
}
