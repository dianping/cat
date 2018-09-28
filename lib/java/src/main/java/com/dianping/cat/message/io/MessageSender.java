package com.dianping.cat.message.io;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageSender {

    void send(MessageTree tree);

    void shutdown();
}
