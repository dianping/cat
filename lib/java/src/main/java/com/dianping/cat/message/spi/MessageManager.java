package com.dianping.cat.message.spi;

import com.dianping.cat.configuration.ClientConfigService;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMessageManager;

/**
 * Message manager to help build CAT message.
 * <p>
 * <p>
 * Notes: This method is reserved for internal usage only. Application developer should never call this method directly.
 */
public interface MessageManager {
    void add(Message message);

    void end(Transaction transaction);

    ClientConfigService getConfigService();

    String getDomain();

    Transaction getPeekTransaction();

    MessageTree getThreadLocalMessageTree();

    boolean hasContext();

    boolean isCatEnabled();

    boolean isMessageEnabled();

    boolean isTraceMode();

    void reset();

    void setTraceMode(boolean traceMode);

    void setup();

    void start(Transaction transaction, boolean forked);

    DefaultMessageManager.Context getContext();

}