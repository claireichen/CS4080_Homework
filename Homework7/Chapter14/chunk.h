#ifndef clox_chunk_h
#define clox_chunk_h

#include "common.h"
#include "value.h"

typedef enum {
  OP_CONSTANT,
  OP_RETURN,
} OpCode;

// Records the byte offset where a new source line begins.
typedef struct {
  int offset;
  int line;
} LineStart;

typedef struct {
  int count;
  int capacity;
  uint8_t* code;
  ValueArray constants;

  // This array has one entry per run of instructions on a source line.
  int lineCount;
  int lineCapacity;
  LineStart* lines;
} Chunk;

void initChunk(Chunk* chunk);
void freeChunk(Chunk* chunk);
void writeChunk(Chunk* chunk, uint8_t byte, int line);
int addConstant(Chunk* chunk, Value value);
int getLine(Chunk* chunk, int instruction);

#endif
