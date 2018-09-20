#include "message_tree.h"

#include "ccat/message.h"

CatMessageTree *copyCatMessageTree(CatMessageTree *pRootMsg) {
    CatMessageTree *pCopyMsg = (CatMessageTree *) malloc(sizeof(CatMessageTree));
    if (pCopyMsg == NULL) {
        return pCopyMsg;
    }
    if (pRootMsg == NULL) {
        memset(pCopyMsg, 0, sizeof(CatMessageTree));
        // default : set canDiscard = 1.
        pCopyMsg->canDiscard = 1;
    } else {
        pCopyMsg->root = pRootMsg->root;
        pCopyMsg->messageId = catsdsdup(pRootMsg->messageId);
        pCopyMsg->parentMessageId = catsdsdup(pRootMsg->parentMessageId);
        pCopyMsg->rootMessageId = catsdsdup(pRootMsg->rootMessageId);
        pCopyMsg->sessionToken = catsdsdup(pRootMsg->sessionToken);
        pCopyMsg->threadGroupName = catsdsdup(pRootMsg->threadGroupName);
        pCopyMsg->threadId = catsdsdup(pRootMsg->threadId);
        pCopyMsg->threadName = catsdsdup(pRootMsg->threadName);
        pCopyMsg->canDiscard = pRootMsg->canDiscard;
    }
    return pCopyMsg;
}

void clearCatMessageTree(CatMessageTree *pRootMsg) {
    pRootMsg->root = NULL;
    if (pRootMsg->messageId != NULL) {
        catsdsfree(pRootMsg->messageId);
        pRootMsg->messageId = NULL;
    }
    if (pRootMsg->parentMessageId != NULL) {
        catsdsfree(pRootMsg->parentMessageId);
        pRootMsg->parentMessageId = NULL;
    }
    if (pRootMsg->rootMessageId != NULL) {
        catsdsfree(pRootMsg->rootMessageId);
        pRootMsg->rootMessageId = NULL;
    }
    if (pRootMsg->sessionToken != NULL) {
        catsdsfree(pRootMsg->sessionToken);
        pRootMsg->sessionToken = NULL;
    }
    if (pRootMsg->threadGroupName != NULL) {
        catsdsfree(pRootMsg->threadGroupName);
        pRootMsg->threadGroupName = NULL;
    }
    if (pRootMsg->threadId != NULL) {
        catsdsfree(pRootMsg->threadId);
        pRootMsg->threadId = NULL;
    }
    if (pRootMsg->threadName != NULL) {
        catsdsfree(pRootMsg->threadName);
        pRootMsg->threadName = NULL;
    }
}

void deleteCatMessageTree(CatMessageTree *pRootMsg) {
    if (pRootMsg->root != NULL) {
        deleteCatMessage(pRootMsg->root);
        pRootMsg->root = NULL;
    }

    clearCatMessageTree(pRootMsg);

    free(pRootMsg);
}

CatMessageTree *createCatMessageTree() {
    return copyCatMessageTree(NULL);
}

