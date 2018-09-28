package com.dianping.cat.message;

import java.io.Closeable;

public interface ForkedTransaction extends Transaction, Closeable {
    void close();

    String getMessageId();

    void setMessageId(String messageId);

    Message join();
}