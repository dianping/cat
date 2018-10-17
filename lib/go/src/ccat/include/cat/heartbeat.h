#ifndef CAT_CLIENT_C_HEARTBEAT_H
#define CAT_CLIENT_C_HEARTBEAT_H

#include "message.h"

typedef CatMessage CatHeartBeat;

CatHeartBeat * createCatHeartBeat(const char *type, const char * name);

#endif //CAT_CLIENT_C_HEARTBEAT_H
