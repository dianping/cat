package com.dianping.cat.message.spi;

import java.util.List;

import io.netty.buffer.ByteBuf;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;

public interface MessageTree extends Cloneable {
	public boolean canDiscard();

	public MessageTree copy();

	public List<Event> findOrCreateEvents();

	public List<Heartbeat> findOrCreateHeartbeats();

	public List<Metric> findOrCreateMetrics();

	public List<Transaction> findOrCreateTransactions();

	public ByteBuf getBuffer();

	public String getDomain();

	public List<Event> getEvents();

	public MessageId getFormatMessageId();

	public List<Heartbeat> getHeartbeats();

	public String getHostName();

	public String getIpAddress();

	public String getSessionToken();

	public Message getMessage();

	public String getMessageId();

	public List<Metric> getMetrics();

	public String getParentMessageId();

	public String getRootMessageId();

	public String getThreadGroupName();

	public String getThreadId();

	public String getThreadName();

	public List<Transaction> getTransactions();

	public boolean isProcessLoss();

	public void setDiscard(boolean discard);

	public void setDomain(String domain);

	public void setFormatMessageId(MessageId messageId);

	public void setHostName(String hostName);

	public void setIpAddress(String ipAddress);

	public void setMessage(Message message);

	public void setMessageId(String messageId);

	public void setSessionToken(String session);

	public void setParentMessageId(String parentMessageId);

	public void setProcessLoss(boolean loss);

	public void setRootMessageId(String rootMessageId);

	public void setThreadGroupName(String name);

	public void setThreadId(String threadId);

	public void setThreadName(String id);

	public boolean isHitSample();

	public void setHitSample(boolean hitSample);

	public void setDiscardPrivate(boolean discard);

}
