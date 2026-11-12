#include <stdio.h>

#include "native_error_test.h"
#include "vm.h"

bool runNativeErrorTest(void) {
  printf("valid sqrt output:\n");
  bool validPassed = interpret("print sqrt(81);") == INTERPRET_OK;

  printf("string error reported: ");
  bool stringRejected =
      interpret("sqrt(\"nine\");") == INTERPRET_RUNTIME_ERROR;
  printf("%s\n", stringRejected ? "yes" : "no");

  printf("negative error reported: ");
  bool negativeRejected =
      interpret("sqrt(-1);") == INTERPRET_RUNTIME_ERROR;
  printf("%s\n", negativeRejected ? "yes" : "no");

  printf("recovery output:\n");
  bool recovered = interpret("print \"ok\";") == INTERPRET_OK;
  printf("VM recovered after errors: %s\n", recovered ? "yes" : "no");

  return validPassed && stringRejected && negativeRejected && recovered;
}
