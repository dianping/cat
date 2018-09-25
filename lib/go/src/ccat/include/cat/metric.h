#ifndef CAT_CLIENT_C_METRIC_H
#define CAT_CLIENT_C_METRIC_H

#include "message.h"

typedef CatMessage CatMetric;

CatMetric *createCatMetric(const char *type, const char *name);

#endif //CAT_CLIENT_C_METRIC_H
