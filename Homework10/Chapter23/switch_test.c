#include <stdio.h>

#include "switch_test.h"
#include "vm.h"

bool runSwitchTest(void) {
  const char* source =
      "var choice = 2;\n"
      "switch (choice) {\n"
      "  case 1:\n"
      "    print \"one\";\n"
      "  case 1 + 1:\n"
      "    var message = \"two\";\n"
      "    print message;\n"
      "  default:\n"
      "    print \"unexpected fallthrough\";\n"
      "}\n"
      "switch (\"missing\") {\n"
      "  case \"found\": print \"wrong case\";\n"
      "  default:\n"
      "    var message = \"default\";\n"
      "    print message;\n"
      "}\n"
      "switch (99) {\n"
      "  case 1: print \"wrong no-match case\";\n"
      "}\n"
      "print \"done\";\n";

  printf("switch output:\n");
  return interpret(source) == INTERPRET_OK;
}
