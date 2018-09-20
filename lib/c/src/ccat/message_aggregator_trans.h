//
// Created by Terence on 2018/9/17.
//

#ifndef CCAT_MESSAGE_AGGREGATOR_TRANS_H
#define CCAT_MESSAGE_AGGREGATOR_TRANS_H

#include <ccat/client.h>

void addTransToAggregator(CatTransaction *pEvent);

void sendTransData();

void initCatTransAggregator();

void destroyCatTransAggregator();

#endif //CCAT_MESSAGE_AGGREGATOR_TRANS_H
