#ifndef CAT_CLIENT_C_MESSAGE_ID_H
#define CAT_CLIENT_C_MESSAGE_ID_H

#include <stdio.h>

#include "lib/cat_sds.h"

void initMessageIdHelper();

void destroyMessageIdHelper();

void flushMessageIdMark();

sds getNextMessageId();

sds getNextMessageIdByAppkey(const char *domain);

void saveMark();

#endif //CAT_CLIENT_C_MESSAGE_ID_H
