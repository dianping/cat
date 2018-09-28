package com.dianping.cat.message.spi;

import com.dianping.cat.message.*;
import com.dianping.cat.message.internal.MessageId;
import io.netty.buffer.ByteBuf;

import java.util.List;

public interface MessageTree extends Cloneable {
    void addForkableTransaction(ForkableTransaction forkableTransaction);

    boolean canDiscard();

    MessageTree copy();

    List<Event> findOrCreateEvents();

    List<Heartbeat> findOrCreateHeartbeats();

    List<Metric> findOrCreateMetrics();

    List<Transaction> findOrCreateTransactions();

    ByteBuf getBuffer();

    String getDomain();

    List<Event> getEvents();

    List<ForkableTransaction> getForkableTransactions();

    MessageId getFormatMessageId();

    List<Heartbeat> getHeartbeats();

    String getHostName();

    String getIpAddress();

    Message getMessage();

    String getMessageId();

    List<Metric> getMetrics();

    String getParentMessageId();

    String getRootMessageId();

    String getSessionToken();

    String getThreadGroupName();

    String getThreadId();

    String getThreadName();

    List<Transaction> getTransactions();

    boolean isHitSample();

    void setDiscardPrivate(boolean discard);

    void setDomain(String domain);

    void setFormatMessageId(MessageId messageId);

    void setHitSample(boolean hitSample);

    void setHostName(String hostName);

    void setIpAddress(String ipAddress);

    void setMessage(Message message);

    void setMessageId(String messageId);

    void setParentMessageId(String parentMessageId);

    void setRootMessageId(String rootMessageId);

    void setSessionToken(String session);

    void setThreadGroupName(String name);

    void setThreadId(String threadId);

    void setThreadName(String id);

}
