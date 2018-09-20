#ifndef CAT_CLIENT_C_CONTEXT_H
#define CAT_CLIENT_C_CONTEXT_H

#include "message_tree.h"

#include "lib/cat_static_queue.h"

typedef struct _CatContext {
    CatMessageTree *tree;
    CATStaticStack *transactionStack;
    int elementSize;
    unsigned long long lastTruncateTransDurationUs;
} CatContext;

CatContext *getCatContext();

CatMessageTree *getContextMessageTree();

void resetCatContext();

void catContextAdd(CatMessage *message);

void catContextAddTransChild(CatMessage *message, CatTransaction *trans);

void catContextAdjustForTruncatedTrans(CatTransaction *root);

void catContextStartTrans(CatTransaction *trans);

int catContextEndTrans(CatTransaction *trans);

CatTransaction *catContextPeekTransaction();


#endif //CAT_CLIENT_C_CONTEXT_H
