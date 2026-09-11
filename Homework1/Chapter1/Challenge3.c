#include <assert.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct Node {
  char *value;
  struct Node *previous;
  struct Node *next;
} Node;

typedef struct {
  Node *head;
  Node *tail;
  size_t size;
} StringList;

static char *copy_string(const char *source) {
  size_t length = strlen(source) + 1;
  char *copy = malloc(length);
  if (copy != NULL) {
    memcpy(copy, source, length);
  }
  return copy;
}

static void list_init(StringList *list) {
  list->head = NULL;
  list->tail = NULL;
  list->size = 0;
}

static bool list_insert(StringList *list, const char *value) {
  Node *node = malloc(sizeof(*node));
  if (node == NULL) {
    return false;
  }

  node->value = copy_string(value);
  if (node->value == NULL) {
    free(node);
    return false;
  }

  node->previous = list->tail;
  node->next = NULL;
  if (list->tail != NULL) {
    list->tail->next = node;
  } else {
    list->head = node;
  }
  list->tail = node;
  list->size++;
  return true;
}

static Node *list_find(const StringList *list, const char *value) {
  for (Node *node = list->head; node != NULL; node = node->next) {
    if (strcmp(node->value, value) == 0) {
      return node;
    }
  }
  return NULL;
}

static bool list_delete(StringList *list, const char *value) {
  Node *node = list_find(list, value);
  if (node == NULL) {
    return false;
  }

  if (node->previous != NULL) {
    node->previous->next = node->next;
  } else {
    list->head = node->next;
  }

  if (node->next != NULL) {
    node->next->previous = node->previous;
  } else {
    list->tail = node->previous;
  }

  free(node->value);
  free(node);
  list->size--;
  return true;
}

static void list_destroy(StringList *list) {
  Node *node = list->head;
  while (node != NULL) {
    Node *next = node->next;
    free(node->value);
    free(node);
    node = next;
  }
  list_init(list);
}

int main(void) {
  StringList list;
  list_init(&list);

  assert(list_insert(&list, "alpha"));
  assert(list_insert(&list, "beta"));
  assert(list_insert(&list, "gamma"));
  assert(list.size == 3);
  assert(list_find(&list, "beta") != NULL);
  assert(list_find(&list, "missing") == NULL);

  assert(list_delete(&list, "alpha"));
  assert(list.head != NULL && strcmp(list.head->value, "beta") == 0);
  assert(list.head->previous == NULL);

  assert(list_delete(&list, "gamma"));
  assert(list.tail != NULL && strcmp(list.tail->value, "beta") == 0);
  assert(list.tail->next == NULL);

  assert(list_delete(&list, "beta"));
  assert(list.head == NULL && list.tail == NULL && list.size == 0);
  assert(!list_delete(&list, "missing"));

  assert(list_insert(&list, "heap-allocated string"));
  list_destroy(&list);
  assert(list.head == NULL && list.tail == NULL && list.size == 0);

  puts("All doubly linked list tests passed.");
  return 0;
}
