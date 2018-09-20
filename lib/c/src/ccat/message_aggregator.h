//
// Created by Terence on 2018/9/17.
//

#ifndef CCAT_MESSAGE_AGGREGATOR_H
#define CCAT_MESSAGE_AGGREGATOR_H

#include "message_tree.h"

void analyzerProcessTransaction(CatTransaction *pTransaction);

void sendToAggregator(CatMessageTree *pMsgTree);

void initCatAggregatorThread();

void clearCatAggregatorThread();

int hitSample();

void setSampleRate(double sampleRate);

#endif //CCAT_MESSAGE_AGGREGATOR_H
