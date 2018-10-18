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

#ifndef CCAT_CAT_STACK_H
#define CCAT_CAT_STACK_H

#include <stdlib.h>

#define CAT_STACK_PUSH_SUCCESS 0

typedef struct _stack CatStack;

struct _stack {
    void* (*peek)(CatStack *s);
    int (*size)(CatStack *s);
    int (*capacity)(CatStack *s);
    int (*push)(CatStack *s, void* data);
    void* (*pop)(CatStack *s);
};

#ifdef __cplusplus
extern "C" {
#endif

CatStack *newCatStack(int capacity);

void deleteCatStack(CatStack *stack);

#ifdef __cplusplus
}
#endif

#endif //CCAT_CAT_STACK_H

