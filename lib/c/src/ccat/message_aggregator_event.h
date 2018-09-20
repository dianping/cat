//
// Created by Terence on 2018/9/17.
//

#ifndef CCAT_MESSAGE_AGGREGATOR_EVENT_H
#define CCAT_MESSAGE_AGGREGATOR_EVENT_H

#include <ccat/client.h>

void addEventToAggregator(CatEvent * pEvent);

void sendEventData();

void initCatEventAggregator();

void destroyCatEventAggregator();

#endif //CCAT_MESSAGE_AGGREGATOR_EVENT_H
