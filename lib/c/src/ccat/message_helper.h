#ifndef CAT_CLIENT_C_TRANSACTION_HELPER_H
#define CAT_CLIENT_C_TRANSACTION_HELPER_H

#include "ccat/context.h"

int isCatTransaction(CatMessage *message);

int isCatEvent(CatMessage *message);

int isCatMetric(CatMessage *message);

int isCatHeartBeat(CatMessage *message);

void truncateAndFlush(CatContext *context, unsigned long long timestampMs);

void validateTransaction(CatTransaction *pParentTrans, CatTransaction *pTrans);

#endif //CAT_CLIENT_C_TRANSACTION_HELPER_H
