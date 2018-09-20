//
// Created by Terence on 2018/9/18.
//

#ifndef CCAT_MESSAGE_AGGREGATOR_METRIC_H
#define CCAT_MESSAGE_AGGREGATOR_METRIC_H

void addCountMetricToAggregator(const char *name, int count);

void addDurationMetricToAggregator(const char *name, int timeMs);

void sendMetricData();

void initCatMetricAggregator();

void destroyCatMetricAggregator();

#endif //CCAT_MESSAGE_AGGREGATOR_METRIC_H
