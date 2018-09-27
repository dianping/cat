package com.dianping.cat.message.spi.internal;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.internal.NullMessage;
import com.dianping.cat.message.spi.MessageTree;

/**
	* Created by yj.huang on 15-8-4.
	*/
public class NullMessageTree implements MessageTree {
	public static final NullMessageTree NULL_MESSAGE_TREE = new NullMessageTree();

	public static final String UNKNOWN = "Unknown";

	public static final String UNKNOWN_MESSAGE_ID = UNKNOWN + "-00000000-000000-0";

	@Override
	public boolean canDiscard() {
		return false;
	}

	@Override
	public MessageTree copy() {
		return this;
	}

	@Override
	public List<Event> findOrCreateEvents() {
		return new ArrayList<Event>();
	}

	@Override
	public List<Heartbeat> findOrCreateHeartbeats() {
		return new ArrayList<Heartbeat>();
	}

	@Override
	public List<Metric> findOrCreateMetrics() {
		return new ArrayList<Metric>();
	}

	@Override
	public List<Transaction> findOrCreateTransactions() {
		return new ArrayList<Transaction>();
	}

	@Override
	public String getDomain() {
		return UNKNOWN;
	}

	@Override
	public List<Event> getEvents() {
		return new ArrayList<Event>();
	}

	@Override
	public List<Heartbeat> getHeartbeats() {
		return new ArrayList<Heartbeat>();
	}

	@Override
	public String getHostName() {
		return UNKNOWN;
	}

	@Override
	public String getIpAddress() {
		return "0.0.0.0";
	}

	@Override
	public Message getMessage() {
		return NullMessage.TRANSACTION;
	}

	@Override
	public String getMessageId() {
		// cat-0a08722f-399628-8613
		return UNKNOWN_MESSAGE_ID;
	}

	@Override
	public List<Metric> getMetrics() {
		return new ArrayList<Metric>();
	}

	@Override
	public String getParentMessageId() {
		return UNKNOWN_MESSAGE_ID;
	}

	@Override
	public String getRootMessageId() {
		return UNKNOWN_MESSAGE_ID;
	}

	@Override
	public String getSessionToken() {
		return UNKNOWN;
	}

	@Override
	public String getThreadGroupName() {
		return UNKNOWN;
	}

	@Override
	public String getThreadId() {
		return "0";
	}

	@Override
	public String getThreadName() {
		return UNKNOWN;
	}

	@Override
	public List<Transaction> getTransactions() {
		return new ArrayList<Transaction>();
	}

	@Override
	public boolean isProcessLoss() {
		return false;
	}

	@Override
	public void setDomain(String domain) {

	}

	@Override
	public void setHostName(String hostName) {

	}

	@Override
	public void setIpAddress(String ipAddress) {

	}

	@Override
	public void setMessage(Message message) {

	}

	@Override
	public void setMessageId(String messageId) {

	}

	@Override
	public void setParentMessageId(String parentMessageId) {

	}

	@Override
	public void setProcessLoss(boolean loss) {
	}

	@Override
	public void setRootMessageId(String rootMessageId) {

	}

	@Override
	public void setDiscard(boolean sample) {

	}

	@Override
	public void setSessionToken(String sessionToken) {

	}

	@Override
	public void setThreadGroupName(String name) {

	}

	@Override
	public void setThreadId(String threadId) {

	}

	@Override
	public void setThreadName(String id) {

	}

	@Override
	public void setHitSample(boolean hitSample) {

	}

	@Override
	public boolean isHitSample() {
		return false;
	}

	@Override
	public ByteBuf getBuffer() {
		return null;
	}

	@Override
	public MessageId getFormatMessageId() {
		return null;
	}

	@Override
	public void setFormatMessageId(MessageId messageId) {
	}

	@Override
	public void setDiscardPrivate(boolean sample) {
	}

}
