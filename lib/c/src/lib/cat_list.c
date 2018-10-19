/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "cat_list.h"

typedef struct _chunk Chunk;

struct _chunk {
    union {
        int h1;
        struct {
            uint16_t capacity;
            uint16_t size;
        } h2;
    };
    Chunk *next;
    void *data[];
};

typedef struct _listInner {
    Chunk* head;
    Chunk* end;
} CatListInner;

#define CAT_LIST_GET_INNER(list) (CatListInner *) ((list) + sizeof(CatList))

Chunk* newChunk(int capacity) {
    Chunk* chunk = calloc(sizeof(Chunk) + capacity * sizeof(void*), 0);
    chunk->h2.capacity = (uint8_t) capacity;
}

int catListSize(CatList* list) {
    CatListInner *l = CAT_LIST_GET_INNER(list);
    int size = 0;
    Chunk* node = l->head;
    while (NULL != node) {
        size += node->h2.size;
        node = node->next;
    }
    return size;
}

int catListPush(CatList* list, void* item) {
    CatListInner *l = CAT_LIST_GET_INNER(list);

    if (NULL == l->head) {
        l->head = l->end = newChunk(8);
    }

    Chunk* end = l->end;
    if (end->h2.size < end->h2.capacity) {
        end->data[end->h2.size] = item;
    } else {
        end->next = newChunk(8);
        end->data[0] = item;
    }
    return 0;
}

CatList* newCatList() {
    int base = sizeof(CatList) + sizeof(CatListInner);
    CatList *list = calloc((size_t) base, 0);

    list->size = catListSize;
    list->push = catListPush;

    return list;
}

void deleteCatList(CatList* list) {

}
