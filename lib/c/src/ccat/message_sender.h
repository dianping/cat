#ifndef CAT_CLIENT_C_MESSAGE_SENDER_H
#define CAT_CLIENT_C_MESSAGE_SENDER_H

#include "message_tree.h"

void initCatSenderThread();

void clearCatSenderThread();

int isCatSenderEnable();

int sendRootMessage(CatMessageTree *tree);

#endif //CAT_CLIENT_C_MESSAGE_SENDER_H
