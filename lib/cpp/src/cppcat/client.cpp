//
// Created by Terence on 2018/8/2.
//

#include "client.hpp"

#include <client.h>
#include <ccat/version.h>

using namespace std;

namespace cat {
    void init(const string& domain) {
        catClientInit(domain.c_str());
    }

    void init(const string& domain, const Config& config) {
        CatClientConfig conf = DEFAULT_CCAT_CONFIG;
        conf.encoderType = config.encoderType;
        conf.enableSampling = config.enableSampling;
        conf.enableMultiprocessing = config.enableMultiprocessing;
        conf.enableHeartbeat = config.enableHeartbeat;
        conf.enableDebugLog = config.enableDebugLog;
        catClientInitWithConfig(domain.c_str(), &conf);
    }

    string version() {
        return string(CPPCAT_VERSION);
    }

    void destroy() {
        catClientDestroy();
    }
}
