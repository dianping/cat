#include "context.h"

#include "client_config.h"
#include "message.h"
#include "message_manager.h"
#include "functions.h"

#include <lib/cat_thread.h>
#include <lib/cat_time_util.h>

extern CatMessageManager g_cat_messageManager;

CATTHREADLOCAL CatContext *g_cat_context = NULL;

CatContext *getCatContext() {
    if (g_cat_context == NULL) {
        g_cat_context = (CatContext *) malloc(sizeof(CatContext));
        catChecktPtr(g_cat_context);
        if (g_cat_context == NULL) {
            return NULL;
        }
        g_cat_context->lastTruncateTransDurationUs = 0;
        g_cat_context->elementSize = 0;
        g_cat_context->tree = createCatMessageTree();
        catChecktPtr(g_cat_context->tree);
        g_cat_context->transactionStack = createCATStaticStack(g_config.maxContextElementSize);
        catChecktPtr(g_cat_context->transactionStack);
        if (g_cat_context->tree != NULL) {
            g_cat_context->tree->root = NULL;

            int pid = cat_get_current_thread_id();
            char tmpBuf[32];
            g_cat_context->tree->threadId = catsdsnew(catItoA(pid, tmpBuf, 10));
            g_cat_context->tree->threadGroupName = catsdsnew("cat");
            g_cat_context->tree->threadName = catsdsnew("cat");
            catChecktPtr(g_cat_context->tree->threadName);
        }
    }
    return g_cat_context;
}

CatMessageTree *getContextMessageTree() {
    return getCatContext()->tree;
}

void resetCatContext() {
    g_cat_context->elementSize = 0;
    g_cat_context->lastTruncateTransDurationUs = 0;
    clearCATStaticStack(g_cat_context->transactionStack);
}

void catContextAdd(CatMessage *message) {
    getCatContext();
    CATStaticStack *pStack = g_cat_context->transactionStack;

    if (isCATStaticStackEmpty(pStack)) {
        CatMessageTree *pRootCopy = copyCatMessageTree(g_cat_context->tree);
        pRootCopy->root = message;
        catMessageManagerFlush(pRootCopy);
    } else {
        CatTransaction *parent = peekCATStaticStack(pStack);

        catContextAddTransChild(message, parent);
    }
}

void catContextAddTransChild(CatMessage *message, CatTransaction *trans) {
    unsigned long long treePeriod = catTrimToHour(getCatMessageTimeStamp(g_cat_context->tree->root));
    // 10 seconds extra time allowed
    unsigned long long messagePeriod = catTrimToHour(getCatMessageTimeStamp(message) - 10 * 1000L);

    if (treePeriod < messagePeriod || g_cat_context->elementSize >= g_config.maxContextElementSize) {
        truncateAndFlush(g_cat_context, getCatMessageTimeStamp(message));
    }

    trans->addChild(trans, message);
    ++g_cat_context->elementSize;
}

void catContextAdjustForTruncatedTrans(CatTransaction *root) {
    CatEvent *next = createCatEvent("TruncatedTransaction", "TotalDuration");
    unsigned long long actualDurationUs =
            g_cat_context->lastTruncateTransDurationUs + getCatTransactionDurationUs(root);

    next->addData(next, catsdsfromlonglong(actualDurationUs));
    next->setStatus(next, CAT_SUCCESS);
    root->addChild(root, next);

    g_cat_context->lastTruncateTransDurationUs = 0;
}

void catContextStartTrans(CatTransaction *trans) {
    getCatContext();
    if (!isCATStaticStackEmpty(g_cat_context->transactionStack)) {
        CatTransaction *parent = peekCATStaticStack(g_cat_context->transactionStack);
        catContextAddTransChild((CatMessage *) trans, parent);
    } else {
        CatMessageTree *t = g_cat_context->tree;
        g_cat_context->tree->root = (CatMessage *) trans;
    }

    pushCATStaticStack(g_cat_context->transactionStack, trans);
}

int catContextEndTrans(CatTransaction *trans) {
    getCatContext();
    size_t i, j;
    size_t initStackSize = getCATStaticStackSize(g_cat_context->transactionStack);

    for (i = 0; i < initStackSize; ++i) {
        CatTransaction *stackTrans = getCATStaticStackByIndex(g_cat_context->transactionStack, i);
        if (stackTrans == trans) {
            break;
        }
    }
    if (i != initStackSize) {
        for (j = 0; j <= i; ++j) {
            popCATStaticStack(g_cat_context->transactionStack);
        }
        validateTransaction(NULL, trans);
        if (isCATStaticStackEmpty(g_cat_context->transactionStack)) {
            CatMessageTree *pCopyRoot = copyCatMessageTree(g_cat_context->tree);

            catsdsfree(g_cat_context->tree->messageId);
            g_cat_context->tree->messageId = NULL;
            g_cat_context->tree->root = NULL;

            if (g_cat_context->lastTruncateTransDurationUs > 0) {
                catContextAdjustForTruncatedTrans((CatTransaction *) pCopyRoot->root);
            }

            catMessageManagerFlush(pCopyRoot);
            return 1;
        }
    }
    return 0;
}

