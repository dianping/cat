package com.dianping.cat.message.spi;

public interface MessageQueue {
    boolean offer(MessageTree tree);

    MessageTree peek();

    MessageTree poll();

    int size();
}
