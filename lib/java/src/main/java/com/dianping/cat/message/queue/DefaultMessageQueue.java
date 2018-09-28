package com.dianping.cat.message.queue;

import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class DefaultMessageQueue implements MessageQueue {

    private BlockingQueue<MessageTree> queue;

    public DefaultMessageQueue(int size) {
        queue = new ArrayBlockingQueue<MessageTree>(size);
    }

    @Override
    public boolean offer(MessageTree tree) {
        return queue.offer(tree);
    }

    @Override
    public MessageTree peek() {
        return queue.peek();
    }

    @Override
    public MessageTree poll() {
        try {
            return queue.poll(5, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }

    @Override
    public int size() {
        return queue.size();
    }
}
