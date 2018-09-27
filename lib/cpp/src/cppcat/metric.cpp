//
// Created by Terence on 2018/8/2.
//

#include "client.hpp"

#include <client.h>

using namespace std;

namespace cat {
    void logMetricForCount(const string& key, unsigned int count) {
        ::logMetricForCount(key.c_str(), count);
    }

    void logMetricForDuration(const string& key, unsigned long ms) {
        ::logMetricForDuration(key.c_str(), ms);
    }
}
