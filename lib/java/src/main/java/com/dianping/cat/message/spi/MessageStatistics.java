package com.dianping.cat.message.spi;

import java.util.Map;

public interface MessageStatistics {

    Map<String, Long> getStatistics();

    void onBytes(int size);

    void onOverflowed(MessageTree tree);

}
