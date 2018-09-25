#ifndef CAT_CLIENT_C_EVENT_H
#define CAT_CLIENT_C_EVENT_H

#include "message.h"

typedef CatMessage CatEvent;

CatEvent * createCatEvent(const char *type, const char * name);

#endif //CAT_CLIENT_C_EVENT_H
