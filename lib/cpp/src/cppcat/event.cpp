//
// Created by Terence on 2018/8/2.
//

#include "client.hpp"

#include <ccat/message.h>

#define E(e) ((CatEvent*)(e))

using namespace std;

namespace cat {
    Event::Event(const string& type, const string& name) {
        event = newEvent(type.c_str(), name.c_str());
    }

    void Event::Complete() {
        E(event)->complete(E(event));
    }

    string Event::GetType() {
        CatMessageInner* inner = getInnerMsg(E(event));
        return string(inner->type);
    }

    string Event::GetName() {
        CatMessageInner* inner = getInnerMsg(E(event));
        return string(inner->name);
    }

    string Event::GetStatus() {
        CatMessageInner* inner = getInnerMsg(E(event));
        return nullptr != inner->status ? string(inner->status) : string();
    }

    void Event::SetStatus(const string& status) {
        E(event)->setStatus(E(event), status.c_str());
    }

    void Event::AddData(const string& data) {
        E(event)->addData(E(event), data.c_str());
    }

    void Event::AddData(const string& key, const string& val) {
        E(event)->addKV(E(event), key.c_str(), val.c_str());
    }

    void Event::SetTimestamp(unsigned long timestamp) {
        E(event)->setTimestamp(E(event), timestamp);
    }

    void logEvent(const string& type, const string& name, const string& status, const string& data) {
        ::logEvent(type.c_str(), name.c_str(), status.c_str(), data.c_str());
    }
}
