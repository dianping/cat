package com.dianping.cat.message.internal;


import com.dianping.cat.configuration.ClientConfigService;
import com.dianping.cat.configuration.DefaultClientConfigService;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.NullMessageTree;

public class NullMessageManager implements MessageManager {

    public static final NullMessageManager NULL_MESSAGE_MANAGER = new NullMessageManager();

    @Override
    public void add(Message message) {
    }

    @Override
    public void end(Transaction transaction) {

    }

    @Override
    public ClientConfigService getConfigService() {
        return DefaultClientConfigService.getInstance();
    }

    @Override
    public DefaultMessageManager.Context getContext() {
        return null;
    }

    @Override
    public String getDomain() {
        return NullMessageTree.NULL_MESSAGE_TREE.getDomain();
    }

    @Override
    public Transaction getPeekTransaction() {
        return NullMessage.TRANSACTION;
    }

    @Override
    public MessageTree getThreadLocalMessageTree() {
        return NullMessageTree.NULL_MESSAGE_TREE;
    }

    @Override
    public boolean hasContext() {
        return false;
    }

    @Override
    public boolean isCatEnabled() {
        return false;
    }

    @Override
    public boolean isMessageEnabled() {
        return false;
    }

    @Override
    public boolean isTraceMode() {
        return false;
    }

    @Override
    public void reset() {
    }

    @Override
    public void setTraceMode(boolean traceMode) {
    }

    @Override
    public void setup() {
    }

    @Override
    public void start(Transaction transaction, boolean forked) {
    }

}
