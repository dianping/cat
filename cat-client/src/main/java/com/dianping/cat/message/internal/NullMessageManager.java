package com.dianping.cat.message.internal;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.NullMessageTree;

public class NullMessageManager implements MessageManager, Initializable, LogEnabled {

	public static final NullMessageManager NULL_MESSAGE_MANAGER = new NullMessageManager();

	@Override
	public void initialize() throws InitializationException {
	}

	@Override
	public void enableLogging(Logger logger) {
	}

	@Override
	public void add(Message message) {
	}

	@Override
	public void end(Transaction transaction) {

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
	public boolean isMessageEnabled() {
		return false;
	}

	@Override
	public boolean isCatEnabled() {
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

	@Override
	public void bind(String tag, String title) {

	}

	@Override
	public String getDomain() {
		return NullMessageTree.NULL_MESSAGE_TREE.getDomain();
	}
}
