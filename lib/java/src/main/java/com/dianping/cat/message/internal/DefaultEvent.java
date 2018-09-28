package com.dianping.cat.message.internal;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.spi.MessageManager;

public class DefaultEvent extends AbstractMessage implements Event {
    private MessageManager manager;

    public DefaultEvent(String type, String name) {
        super(type, name);
    }

    DefaultEvent(String type, String name, MessageManager manager) {
        super(type, name);

        this.manager = manager;
    }

    @Override
    public void complete() {
        setCompleted(true);

        if (manager != null) {
            manager.add(this);
        }
    }
}
