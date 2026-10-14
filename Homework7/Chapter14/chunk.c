#include "chunk.h"
#include "memory.h"

void initChunk(Chunk* chunk) {
  chunk->count = 0;
  chunk->capacity = 0;
  chunk->code = NULL;
  initValueArray(&chunk->constants);

  chunk->lineCount = 0;
  chunk->lineCapacity = 0;
  chunk->lines = NULL;
}

void freeChunk(Chunk* chunk) {
  FREE_ARRAY(uint8_t, chunk->code, chunk->capacity);
  FREE_ARRAY(LineStart, chunk->lines, chunk->lineCapacity);
  freeValueArray(&chunk->constants);
  initChunk(chunk);
}

void writeChunk(Chunk* chunk, uint8_t byte, int line) {
  if (chunk->capacity < chunk->count + 1) {
    int oldCapacity = chunk->capacity;
    chunk->capacity = GROW_CAPACITY(oldCapacity);
    chunk->code = GROW_ARRAY(uint8_t, chunk->code,
                            oldCapacity, chunk->capacity);
  }

  chunk->code[chunk->count] = byte;
  chunk->count++;

  // Do not add another entry while instructions remain on the same line.
  if (chunk->lineCount > 0 &&
      chunk->lines[chunk->lineCount - 1].line == line) {
    return;
  }

  if (chunk->lineCapacity < chunk->lineCount + 1) {
    int oldCapacity = chunk->lineCapacity;
    chunk->lineCapacity = GROW_CAPACITY(oldCapacity);
    chunk->lines = GROW_ARRAY(LineStart, chunk->lines,
                             oldCapacity, chunk->lineCapacity);
  }

  LineStart* lineStart = &chunk->lines[chunk->lineCount++];
  lineStart->offset = chunk->count - 1;
  lineStart->line = line;
}

int addConstant(Chunk* chunk, Value value) {
  writeValueArray(&chunk->constants, value);
  return chunk->constants.count - 1;
}

int getLine(Chunk* chunk, int instruction) {
  if (instruction < 0 || instruction >= chunk->count ||
      chunk->lineCount == 0) {
    return -1;
  }

  int start = 0;
  int end = chunk->lineCount - 1;

  while (start <= end) {
    int mid = start + (end - start) / 2;
    LineStart* current = &chunk->lines[mid];

    if (instruction < current->offset) {
      end = mid - 1;
    } else if (mid == chunk->lineCount - 1 ||
               instruction < chunk->lines[mid + 1].offset) {
      return current->line;
    } else {
      start = mid + 1;
    }
  }

  return -1;
}
