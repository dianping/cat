#include "cat/client.h"
#include "cat/metric_helper.h"

void callAddTag(CatMetricHelper *helper, const char* key, const char* val) {
  helper->AddTag(helper, key, val);
}

void callCount(CatMetricHelper *helper, int c) {
  helper->AddCount(helper, c);
}

void callDuration(CatMetricHelper *helper, int d) {
  helper->AddDuration(helper, d);
}

void callLogTransaction(
    const char* type,
    const char* name,
    const char* status,
    const char* data,
    unsigned long long timestampMs,
    unsigned long long durationStartMs,
    unsigned long long durationMs
    ) {
  CatTransaction *t = newTransaction(type, name);
  t->setStatus((CatMessage*) t, status);
  t->addDataPair((CatMessage*) t, data);
  setTransactionTimestamp(t, timestampMs);
  t->setDurationInMillis(t, durationMs);
  t->setComplete((CatMessage*) t);
  return;
}
