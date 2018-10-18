#ifndef CAT_CLIENT_C_TRANSACTION_H
#define CAT_CLIENT_C_TRANSACTION_H

#include "message.h"

typedef struct _CatTransaction CatTransaction;

struct _CatTransaction {
    void (*addDataPair)(CatMessage *message, const char *data);

    void (*addData)(CatMessage *message, const char *dataKey, const char *dataValue);

    void (*setStatus)(CatMessage *message, const char *status);

    void (*setComplete)(CatMessage *message);

    void *(*clear)(CatMessage *message);

    void (*addChild)(CatTransaction *message, CatMessage *childMsg);

    void (*setDurationInMillis)(CatTransaction *message, unsigned long long durationMs);

    void (*setDurationStart)(CatTransaction *message, unsigned long long durationStart);
};




#endif //CAT_CLIENT_C_TRANSACTION_H
