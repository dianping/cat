#ifndef CAT_CLIENT_C_TRACE_H
#define CAT_CLIENT_C_TRACE_H

#include "message.h"

typedef CatMessage CatTrace;

CatTrace *createCatTrace(const char *type, const char *name);

#endif //CAT_CLIENT_C_TRACE_H
