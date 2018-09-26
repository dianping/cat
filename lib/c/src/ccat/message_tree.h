#ifndef CAT_CLIENT_C_MESSAGE_TREE_H
#define CAT_CLIENT_C_MESSAGE_TREE_H

#include "ccat/client.h"

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
