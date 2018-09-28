package com.dianping.cat.message.spi.internal;

import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.message.spi.MessageTree;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultMessageStatistics implements MessageStatistics {
    private AtomicLong produced = new AtomicLong();
    private AtomicLong overflowed = new AtomicLong();
    private AtomicLong bytes = new AtomicLong();

    @Override
    public void onBytes(int bytes) {
        this.bytes.addAndGet(bytes);
        produced.incrementAndGet();
    }

    @Override
    public void onOverflowed(MessageTree tree) {
        overflowed.incrementAndGet();
    }

    @Override
    public Map<String, Long> getStatistics() {
        Map<String, Long> map = new HashMap<String, Long>();

        map.put("cat.status.message.produced", produced.get());
        produced = new AtomicLong();

        map.put("cat.status.message.overflowed", overflowed.get());
        overflowed = new AtomicLong();

        map.put("cat.status.message.bytes", bytes.get());
        bytes = new AtomicLong();

        return map;
    }
}
