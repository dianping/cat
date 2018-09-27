//
// Created by Terence on 2018/8/2.
//

#include "client.hpp"

#include <ccat/message.h>

using namespace std;

#define T(t) ((CatTransaction*)(t))

namespace cat {
    Transaction::Transaction(const string& type, const string& name) {
        trans = newTransaction(type.c_str(), name.c_str());
    }

    void Transaction::Complete() {
        T(trans)->complete(T(trans));
    }

    unsigned long Transaction::GetDurationMs() {
        CatTransactionInner* inner = getInnerTrans(T(trans));
        return inner->durationUs / 1000;
    }

    string Transaction::GetType() {
        CatMessageInner* inner = getInnerMsg(T(trans));
        return string(inner->type);
    }

    string Transaction::GetName() {
        CatMessageInner* inner = getInnerMsg(T(trans));
        return string(inner->name);
    }

    string Transaction::GetStatus() {
        CatMessageInner* inner = getInnerMsg(T(trans));
        return string(inner->status);
    }

    void Transaction::SetStatus(const string& status) {
        T(trans)->setStatus(T(trans), status.c_str());
    }

    void Transaction::SetDurationStart(unsigned long ms) {
        T(trans)->setDurationStart(T(trans), ms);
    }

    void Transaction::SetDurationInMillis(unsigned long ms) {
        T(trans)->setDurationInMillis(T(trans), ms);
    }

    void Transaction::AddData(const string& data) {
        T(trans)->addData(T(trans), data.c_str());
    }

    void Transaction::AddData(const string& key, const string& val) {
        T(trans)->addKV(T(trans), key.c_str(), val.c_str());
    }

    void Transaction::SetTimestamp(unsigned long timestamp) {
        T(trans)->setTimestamp(T(trans), timestamp);
    }
}

