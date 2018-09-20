#ifndef CAT_CLIENT_C_MONITOR_H
#define CAT_CLIENT_C_MONITOR_H

#define Cat_C_Client_Version "2.1.0"

#include "lib/cat_sds.h"

typedef struct {
    volatile sds language;
    volatile sds language_version;
} CatClientInfo;

void initCatMonitorThread();

void clearCatMonitor();

#endif //CAT_CLIENT_C_MONITOR_H
