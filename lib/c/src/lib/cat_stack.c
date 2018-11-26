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

#include "cat_stack.h"

typedef struct _stackInner {
    size_t capacity;
    size_t length;
    void* data[];
} CatStackInner;

#define CAT_STACK_GET_INNER(stack) (CatStackInner *) ((stack) + sizeof(CatStack))

void* catStackPeek(CatStack* stack) {
    CatStackInner *s = CAT_STACK_GET_INNER(stack);
    if (s->length == 0) {
        return NULL;
    }
    return s->data[s->length - 1];
}

int catStackSize(CatStack* stack) {
    CatStackInner *s = CAT_STACK_GET_INNER(stack);
    return (int) s->length;
}

int catStackCapacity(CatStack* stack) {
    CatStackInner *s = CAT_STACK_GET_INNER(stack);
    return (int) s->capacity;
}

int catStackPush(CatStack* stack, void* item) {
    CatStackInner *s = CAT_STACK_GET_INNER(stack);
    if (s->length < s->capacity) {
        s->data[s->length++] = item;
        return CAT_STACK_PUSH_SUCCESS;
    } else {
        return 1;
    }
}

void* catStackPop(CatStack* stack) {
    CatStackInner *s = CAT_STACK_GET_INNER(stack);
    if (s->length < 1) {
        return NULL;
    }
    return s->data[--s->length];
}

CatStack* newCatStack(int capacity) {
    int base = sizeof(CatStack) + sizeof(CatStackInner);
    CatStack *stack = malloc(base + capacity * sizeof(void *));

    CatStackInner *s = CAT_STACK_GET_INNER(stack);
    s->capacity = (size_t) capacity;
    s->length = 0;

    stack->peek = catStackPeek;
    stack->size = catStackSize;
    stack->capacity = catStackCapacity;
    stack->push = catStackPush;
    stack->pop = catStackPop;

    return stack;
}

void deleteCatStack(CatStack *stack) {
    free(stack);
}
