#include <stdio.h>

#include "hash_key_test.h"
#include "object.h"
#include "table.h"
#include "value.h"

static bool expectValue(Table* table, Value key, Value expected) {
  Value actual;
  return tableGet(table, key, &actual) && valuesEqual(actual, expected);
}

bool runHashKeyTests(void) {
  Table table;
  initTable(&table);

  ObjString* name = copyString("lox", 3);
  tableSet(&table, NUMBER_VAL(42.5), NUMBER_VAL(1));
  tableSet(&table, BOOL_VAL(true), NUMBER_VAL(2));
  tableSet(&table, BOOL_VAL(false), NUMBER_VAL(3));
  tableSet(&table, NIL_VAL, NUMBER_VAL(4));
  tableSet(&table, OBJ_VAL(name), NUMBER_VAL(5));

  bool primitivesPass =
      expectValue(&table, NUMBER_VAL(42.5), NUMBER_VAL(1)) &&
      expectValue(&table, BOOL_VAL(true), NUMBER_VAL(2)) &&
      expectValue(&table, BOOL_VAL(false), NUMBER_VAL(3)) &&
      expectValue(&table, NIL_VAL, NUMBER_VAL(4)) &&
      expectValue(&table, OBJ_VAL(name), NUMBER_VAL(5));

  tableDelete(&table, NIL_VAL);
  Value ignored;
  bool deletionPass = !tableGet(&table, NIL_VAL, &ignored);
  tableSet(&table, NIL_VAL, NUMBER_VAL(6));
  deletionPass = deletionPass &&
      expectValue(&table, NIL_VAL, NUMBER_VAL(6));

  for (int i = 0; i < 100; i++) {
    tableSet(&table, NUMBER_VAL(i), NUMBER_VAL(i * i));
  }

  bool growthPass = true;
  for (int i = 0; i < 100; i++) {
    if (!expectValue(&table, NUMBER_VAL(i), NUMBER_VAL(i * i))) {
      growthPass = false;
      break;
    }
  }

  printf("primitive keys: %s\n", primitivesPass ? "ok" : "failed");
  printf("deletion and reuse: %s\n", deletionPass ? "ok" : "failed");
  printf("growth and lookup: %s\n", growthPass ? "ok" : "failed");

  freeTable(&table);
  return primitivesPass && deletionPass && growthPass;
}
