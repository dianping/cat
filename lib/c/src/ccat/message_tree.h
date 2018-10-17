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
#ifndef CAT_CLIENT_C_MESSAGE_TREE_H
#define CAT_CLIENT_C_MESSAGE_TREE_H

#include "client.h"

#include "lib/cat_sds.h"

typedef struct _CatMessageTree {
    CatMessage *root;
    sds messageId;
    sds parentMessageId;
    sds rootMessageId;
    sds sessionToken;
    sds threadGroupName;
    sds threadId;
    sds threadName;
    int canDiscard;
} CatMessageTree;

CatMessageTree *createCatMessageTree();

CatMessageTree *copyCatMessageTree(CatMessageTree *pMsgTree);

void clearCatMessageTree(CatMessageTree *pMsgTree);

void deleteCatMessageTree(CatMessageTree *pMsgTree);

#endif //CAT_CLIENT_C_MESSAGE_TREE_H
