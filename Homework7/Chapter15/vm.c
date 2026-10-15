#include <stdio.h>

#include "memory.h"
#include "vm.h"

VM vm;

static void resetStack(void) {
  vm.stackCount = 0;
}

void initVM(void) {
  vm.chunk = NULL;
  vm.ip = NULL;
  vm.stack = NULL;
  vm.stackCapacity = 0;
  resetStack();
}

void freeVM(void) {
  FREE_ARRAY(Value, vm.stack, vm.stackCapacity);
  vm.stack = NULL;
  vm.stackCapacity = 0;
  resetStack();
}

void push(Value value) {
  if (vm.stackCapacity < vm.stackCount + 1) {
    int oldCapacity = vm.stackCapacity;
    vm.stackCapacity = GROW_CAPACITY(oldCapacity);
    vm.stack = GROW_ARRAY(Value, vm.stack,
                         oldCapacity, vm.stackCapacity);
  }

  vm.stack[vm.stackCount] = value;
  vm.stackCount++;
}

Value pop(void) {
  vm.stackCount--;
  return vm.stack[vm.stackCount];
}

static InterpretResult run(void) {
#define READ_BYTE() (*vm.ip++)
#define READ_CONSTANT() (vm.chunk->constants.values[READ_BYTE()])
#define READ_LONG() \
  (vm.ip += 3, (uint32_t)(vm.ip[-3] | (vm.ip[-2] << 8) | \
                          (vm.ip[-1] << 16)))
#define READ_CONSTANT_LONG() (vm.chunk->constants.values[READ_LONG()])
#define BINARY_OP(op)        \
  do {                       \
    Value b = pop();         \
    Value a = pop();         \
    push(a op b);            \
  } while (false)

  for (;;) {
    uint8_t instruction = READ_BYTE();
    switch (instruction) {
      case OP_CONSTANT:
        push(READ_CONSTANT());
        break;
      case OP_CONSTANT_LONG:
        push(READ_CONSTANT_LONG());
        break;
      case OP_NEGATE:
        push(-pop());
        break;
      case OP_ADD:
        BINARY_OP(+);
        break;
      case OP_SUBTRACT:
        BINARY_OP(-);
        break;
      case OP_MULTIPLY:
        BINARY_OP(*);
        break;
      case OP_DIVIDE:
        BINARY_OP(/);
        break;
      case OP_RETURN:
        printValue(pop());
        printf("\n");
        return INTERPRET_OK;
      default:
        return INTERPRET_RUNTIME_ERROR;
    }
  }

#undef READ_BYTE
#undef READ_CONSTANT
#undef READ_LONG
#undef READ_CONSTANT_LONG
#undef BINARY_OP
}

InterpretResult interpret(Chunk* chunk) {
  vm.chunk = chunk;
  vm.ip = vm.chunk->code;
  resetStack();
  return run();
}
