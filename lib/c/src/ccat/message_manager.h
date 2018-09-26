#ifndef CAT_CLIENT_C_MESSAGE_MANAGER_H
#define CAT_CLIENT_C_MESSAGE_MANAGER_H

#include "ccat/message_tree.h"

#include "lib/cat_sds.h"

typedef struct _CatMessageManager {
    sds domain;
    sds hostname;
    sds ip;
    sds ipHex;
    long throttleTimes;
} CatMessageManager;

void catMessageManagerAdd(CatMessage *message);

void catMessageManagerEndTrans(CatTransaction *trans);

void catMessageManagerFlush(CatMessageTree *rootMsg);

void initMessageManager(const char *domain, const char *hostName);

void catMessageManagerDestroy();

void catMessageManagerStartTrans(CatTransaction *trans);

#endif //CAT_CLIENT_C_MESSAGE_MANAGER_H
