#include "cat/client.h"

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
  t->setStatus(t, status);
  t->addData(t, data);
  t->setTimestamp(t, timestampMs);
  t->setDurationStart(t, durationStartMs);
  t->setDurationInMillis(t, durationMs);
  t->complete(t);
  return;
}

void callLogEvent(
    const char* type,
    const char* name,
    const char* status,
    const char* data,
    unsigned long long timestampMs
    ) {
  CatMessage *e = newEvent(type, name);
  e->setStatus(e, status);
  e->addData(e, data);
  e->setTimestamp(e, timestampMs);
  e->complete(e);
}
