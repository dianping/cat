#ifndef METRIC_HELPER_H_
#define METRIC_HELPER_H_

typedef struct _CatMetricHelper CatMetricHelper;

struct _CatMetricHelper {
    CatMetricHelper *(*AddName)(CatMetricHelper *pHelper, const char *name);

    CatMetricHelper *(*AddTags)(CatMetricHelper *pHelper, int tagCount, ...);

    CatMetricHelper *(*AddTag)(CatMetricHelper *pHelper, const char *key, const char *val);

    CatMetricHelper *(*AddCount)(CatMetricHelper *pHelper, int count);

    CatMetricHelper *(*AddDuration)(CatMetricHelper *pHelper, unsigned long long durationMs);
};

#ifdef __cplusplus
extern "C" {
#endif

CatMetricHelper *CatBuildMetricHelper(const char *name);

#ifdef __cplusplus
}
#endif

#endif//METRIC_HELPER_H_
