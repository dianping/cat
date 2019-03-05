//
// Created by Terence on 2018/8/2.
//

#include <iostream>
#include <unistd.h>

#include "client.hpp"

using namespace std;

unsigned long long GetTime64() {
    return static_cast<unsigned long long int>(std::chrono::system_clock::now().time_since_epoch().count() / 1000);
}

void transaction() {
    cat::Transaction t("foo", "bar");
    t.AddData("foo", "1");
    t.AddData("bar", "2");
    t.AddData("foo is a bar");
    t.SetDurationStart(GetTime64() - 1000);
    t.SetTimestamp(GetTime64() - 1000);
    t.SetDurationInMillis(150);
    t.SetStatus(cat::FAIL);
    t.Complete();
}

void event() {
    cat::Event e("foo", "bar");
    e.AddData("foo", "1");
    e.AddData("bar", "2");
    e.AddData("foo is a bar");
    e.SetStatus(cat::SUCCESS);
    e.Complete();

    cat::logEvent("foo", "bar1");
    cat::logEvent("foo", "bar2", "failed");
    cat::logEvent("foo", "bar3", "failed", "k=v");
}

void metric() {
    cat::logMetricForCount("count");
    cat::logMetricForCount("count", 3);
    cat::logMetricForDuration("duration", 100);
}

int main() {
    cout << "cppcat version: " << cat::version() << endl;

    cat::Config c = cat::Config();
    c.encoderType = cat::ENCODER_BINARY;
    cat::init("cppcat", c);

    for (int i = 0; i < 100; i++) {
        transaction();
        event();
        metric();
        usleep(10000);
    }
    usleep(1000000);
    cat::destroy();
}