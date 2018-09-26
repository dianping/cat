#ifndef CAT_CLIENT_C_ENCODER_H
#define CAT_CLIENT_C_ENCODER_H

#include "ccat/message_tree.h"

#include "lib/cat_sds.h"

#define CAT_ENCODER_TEXT 0
#define CAT_ENCODER_BINARY 1

typedef struct _CatEncoder CatEncoder;

struct _CatEncoder {
    void (*setAppkey)(CatEncoder *encoder, const char *appkey);

    void (*setHostname)(CatEncoder *encoder, const char *hostname);

    void (*setIp)(CatEncoder *encoder, const char *ip);

    void (*header)(CatEncoder *encoder, CatMessageTree *tree);

    void (*message)(CatEncoder *encoder, CatMessage *message);

    void (*transactionStart)(CatEncoder *encoder, CatTransaction *t);

    void (*transactionEnd)(CatEncoder *encoder, CatTransaction *t);

    void (*transaction)(CatEncoder *encoder, CatTransaction *t);

    void (*event)(CatEncoder *encoder, CatEvent *e);

    void (*metric)(CatEncoder *encoder, CatMetric *m);

    void (*heartbeat)(CatEncoder *encoder, CatHeartBeat *h);

    const char *ip;
    const char *hostname;
    const char *appkey;

    sds* buf;
};

void catEncodeMessageTree(CatMessageTree *tree, sds buf);

CatEncoder *newCatEncoder();

CatEncoder *newCatTextEncoder();

CatEncoder *newCatBinaryEncoder();

void deleteCatEncoder(CatEncoder *encoder);

#endif // CAT_CLIENT_C_ENCODER_H