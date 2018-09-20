//
// Created by Terence on 2018/8/2.
//

#ifndef CCAT_CLIENT_H
#define CCAT_CLIENT_H

#include <string>

namespace cat {

    using namespace std;

    const string SUCCESS = "0";
    const string FAIL = "-1";
    const string ERROR = "ERROR";

    const int ENCODER_TEXT = 0;
    const int ENCODER_BINARY  = 1;

    class Config {
    public:
        int encoderType = ENCODER_BINARY;
        bool enableHeartbeat = true;
        bool enableSampling = true;
        bool enableMultiprocessing = false;
        bool enableDebugLog = false;
    };

    class Transaction {
    private:
        void* trans;
    public:
        Transaction(const string& type, const string& name);

        void Complete();

        unsigned long GetDurationMs();

        string GetType();

        string GetName();

        string GetStatus();

        void SetStatus(const string& status);

        void SetDurationStart(unsigned long ms);

        void SetDurationInMillis(unsigned long ms);

        void SetTimestamp(unsigned long timestamp);

        void AddData(const string& key, const string& val);

        void AddData(const string& data);
    };

    class Event {
    private:
        void* event;
    public:
        Event(const string& type, const string& name);

        void Complete();

        string GetType();

        string GetName();

        string GetStatus();

        void SetStatus(const string& status);

        void SetTimestamp(unsigned long timestamp);

        void AddData(const string& key, const string& val);

        void AddData(const string& data);
    };

    void init(const string& domain);

    void init(const string& domain, const Config& config);

    void destroy();

    void logEvent(const string& type, const string& name, const string& status = SUCCESS, const string& data = "");

    void logMetricForCount(const string& key, unsigned int count = 1);

    void logMetricForDuration(const string& key, unsigned long ms);
};

#endif //CCAT_CLIENT_H
