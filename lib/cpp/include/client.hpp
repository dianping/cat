/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#ifndef CPPCAT_CLIENT_H
#define CPPCAT_CLIENT_H

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

    string version();

    void destroy();

    void logEvent(const string& type, const string& name, const string& status = SUCCESS, const string& data = "");

    void logMetricForCount(const string& key, unsigned int count = 1);

    void logMetricForDuration(const string& key, unsigned long ms);
};

#endif // CPPCAT_CLIENT_H
