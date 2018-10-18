#ifndef CAT_CLIENT_C_MESSAGE_H
#define CAT_CLIENT_C_MESSAGE_H

#include <string.h>
#include <stdlib.h>
#include "client_common.h"

typedef struct _CatMessage CatMessage;



struct _CatMessage {
    void (*addDataPair)(CatMessage *message, const char *data);

    void (*addData)(CatMessage *message, const char *dataKey, const char *dataValue);

    void (*setStatus)(CatMessage *message, const char *status);

    void (*setComplete)(CatMessage *message);

    void *(*clear)(CatMessage *message);
};



#endif //CAT_CLIENT_C_MESSAGE_H
