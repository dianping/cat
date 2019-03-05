/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.message.internal;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.ApplicationSettings;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.TaggedTransaction;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.io.MessageSender;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

@Named(type = MessageManager.class)
public class DefaultMessageManager extends ContainerHolder implements MessageManager, Initializable, LogEnabled {

	@Inject
	private ClientConfigManager m_configManager;

	@Inject
	private TransportManager m_transportManager;

	@Inject
	private MessageIdFactory m_factory;

	private ThreadLocal<Context> m_context = new ThreadLocal<Context>();

	private long m_throttleTimes;

	private Domain m_domain;

	private String m_hostName;

	private boolean m_firstMessage = true;

	private TransactionHelper m_validator = new TransactionHelper();

	private Map<String, TaggedTransaction> m_taggedTransactions;

	private AtomicInteger m_sampleCount = new AtomicInteger();

	private Logger m_logger;

	@Override
	public void add(Message message) {
		Context ctx = getContext();

		if (ctx != null) {
			ctx.add(message);
		}
	}

	@Override
	public void bind(String tag, String title) {
		TaggedTransaction t = m_taggedTransactions.get(tag);

		if (t != null) {
			MessageTree tree = getThreadLocalMessageTree();
			String messageId = tree.getMessageId();

			if (messageId == null) {
				messageId = nextMessageId();
				tree.setMessageId(messageId);
			}
			if (tree != null) {
				t.start();
				t.bind(tag, messageId, title);
			}
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void end(Transaction transaction) {
		Context ctx = getContext();

		if (ctx != null && transaction.isStandalone()) {
			if (ctx.end(this, transaction)) {
				m_context.remove();
			}
		}
	}

	public void flush(MessageTree tree, boolean clearContext) {
		MessageSender sender = m_transportManager.getSender();

		if (sender != null && isMessageEnabled()) {
			sender.send(tree);

			if (clearContext) {
				reset();
			}
		} else {
			m_throttleTimes++;

			if (m_throttleTimes % 10000 == 0 || m_throttleTimes == 1) {
				m_logger.info("Cat Message is throttled! Times:" + m_throttleTimes);
			}
		}
	}

	@Override
	public ClientConfigManager getConfigManager() {
		return m_configManager;
	}

	private Context getContext() {
		if (Cat.isInitialized()) {
			Context ctx = m_context.get();

			if (ctx != null) {
				return ctx;
			} else {
				if (m_domain != null) {
					ctx = new Context(m_domain.getId(), m_hostName, m_domain.getIp());
				} else {
					ctx = new Context("Unknown", m_hostName, "");
				}

				m_context.set(ctx);
				return ctx;
			}
		}

		return null;
	}

	@Override
	public String getDomain() {
		return m_domain.getId();
	}

	public String getMetricType() {
		return "";
	}

	public void setMetricType(String metricType) {
	}

	@Override
	public Transaction getPeekTransaction() {
		Context ctx = getContext();

		if (ctx != null) {
			return ctx.peekTransaction(this);
		} else {
			return null;
		}
	}

	@Override
	public MessageTree getThreadLocalMessageTree() {
		Context ctx = m_context.get();

		if (ctx == null) {
			setup();
		}
		ctx = m_context.get();

		return ctx.m_tree;
	}

	@Override
	public boolean hasContext() {
		Context context = m_context.get();
		boolean has = context != null;

		if (has) {
			MessageTree tree = context.m_tree;

			if (tree == null) {
				return false;
			}
		}
		return has;
	}

	private boolean hitSample(double sampleRatio) {
		int count = m_sampleCount.incrementAndGet();

		return count % ((int) (1.0 / sampleRatio)) == 0;
	}

	@Override
	public void initialize() throws InitializationException {
		m_domain = m_configManager.getDomain();
		m_hostName = NetworkInterfaceManager.INSTANCE.getLocalHostName();

		if (m_domain.getIp() == null) {
			m_domain.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		}

		// initialize domain and IP address
		try {
			m_factory.initialize(m_domain.getId());
		} catch (Exception e) {
			m_logger.error("error when create mark file", e);
		}

		// initialize the tagged transaction cache
		final int size = m_configManager.getTaggedTransactionCacheSize();

		m_taggedTransactions = new LinkedHashMap<String, TaggedTransaction>(size * 4 / 3 + 1, 0.75f, true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Entry<String, TaggedTransaction> eldest) {
				return size() >= size;
			}
		};
	}

	@Override
	public boolean isCatEnabled() {
		return m_domain != null && m_domain.isEnabled() && m_configManager.isCatEnabled();
	}

	@Override
	public boolean isMessageEnabled() {
		return m_domain != null && m_domain.isEnabled() && m_context.get() != null && m_configManager.isCatEnabled();
	}

	public boolean isTraceMode() {
		Context content = getContext();

		if (content != null) {
			return content.isTraceMode();
		} else {
			return false;
		}
	}

	public void setTraceMode(boolean traceMode) {
		Context context = getContext();

		if (context != null) {
			context.setTraceMode(traceMode);
		}
	}

	public void linkAsRunAway(DefaultForkedTransaction transaction) {
		Context ctx = getContext();
		if (ctx != null) {
			ctx.linkAsRunAway(transaction);
		}
	}

	private String nextMessageId() {
		return m_factory.getNextId();
	}

	@Override
	public void reset() {
		// destroy current thread local data
		Context ctx = m_context.get();

		if (ctx != null) {
			if (ctx.m_totalDurationInMicros == 0) {
				ctx.m_stack.clear();
				ctx.m_knownExceptions.clear();
				m_context.remove();
			} else {
				ctx.m_knownExceptions.clear();
			}

			MessageTree tree = ctx.m_tree;
			if (tree != null) {
				double samplingRate = m_configManager.getSampleRatio();

				if (samplingRate < 1.0 && hitSample(samplingRate)) {
					tree.setHitSample(true);
				} else {
					tree.setHitSample(false);
				}
			}
		}
	}

	@Override
	public void setup() {
		Context ctx;

		if (m_domain != null) {
			ctx = new Context(m_domain.getId(), m_hostName, m_domain.getIp());
		} else {
			ctx = new Context("Unknown", m_hostName, "");
		}
		double samplingRate = m_configManager.getSampleRatio();

		if (samplingRate < 1.0 && hitSample(samplingRate)) {
			ctx.m_tree.setHitSample(true);
		}
		m_context.set(ctx);
	}

	boolean shouldLog(Throwable e) {
		Context ctx = m_context.get();

		if (ctx != null) {
			return ctx.shouldLog(e);
		} else {
			return true;
		}
	}

	@Override
	public void start(Transaction transaction, boolean forked) {
		Context ctx = getContext();

		if (ctx != null) {
			ctx.start(transaction, forked);

			if (transaction instanceof TaggedTransaction) {
				TaggedTransaction tt = (TaggedTransaction) transaction;

				m_taggedTransactions.put(tt.getTag(), tt);
			}
		} else if (m_firstMessage) {
			m_firstMessage = false;
			m_logger.warn("CAT client is not enabled because it's not initialized yet");
		}
	}

	class Context {
		private MessageTree m_tree;

		private Stack<Transaction> m_stack;

		private int m_length;

		private boolean m_traceMode;

		private long m_totalDurationInMicros; // for truncate message

		private Set<Throwable> m_knownExceptions;

		public Context(String domain, String hostName, String ipAddress) {
			m_tree = new DefaultMessageTree();
			m_stack = new Stack<Transaction>();

			Thread thread = Thread.currentThread();
			String groupName = thread.getThreadGroup().getName();

			m_tree.setThreadGroupName(groupName);
			m_tree.setThreadId(String.valueOf(thread.getId()));
			m_tree.setThreadName(thread.getName());

			m_tree.setDomain(domain);
			m_tree.setHostName(hostName);
			m_tree.setIpAddress(ipAddress);
			m_length = 1;
			m_knownExceptions = new HashSet<Throwable>();
		}

		public void add(Message message) {
			if (m_stack.isEmpty()) {
				MessageTree tree = m_tree.copy();

				tree.setMessage(message);
				flush(tree, true);
			} else {
				Transaction parent = m_stack.peek();

				addTransactionChild(message, parent);
			}
		}

		private void addTransactionChild(Message message, Transaction transaction) {
			long treePeriod = trimToHour(m_tree.getMessage().getTimestamp());
			long messagePeriod = trimToHour(message.getTimestamp() - 10 * 1000L); // 10 seconds extra time allowed

			if (treePeriod < messagePeriod || m_length >= ApplicationSettings.getTreeLengthLimit()) {
				m_validator.truncateAndFlush(this, message.getTimestamp());
			}

			transaction.addChild(message);
			m_length++;
		}

		private void adjustForTruncatedTransaction(Transaction root) {
			DefaultEvent next = new DefaultEvent("TruncatedTransaction", "TotalDuration");
			long actualDurationInMicros = m_totalDurationInMicros + root.getDurationInMicros();

			next.addData(String.valueOf(actualDurationInMicros));
			next.setStatus(Message.SUCCESS);
			root.addChild(next);

			m_totalDurationInMicros = 0;
		}

		/**
			* return true means the transaction has been flushed.
			*
			* @param manager
			* @param transaction
			* @return true if message is flushed, false otherwise
			*/
		public boolean end(DefaultMessageManager manager, Transaction transaction) {
			if (!m_stack.isEmpty()) {
				Transaction current = m_stack.pop();

				if (transaction == current) {
					m_validator.validate(m_stack.isEmpty() ? null : m_stack.peek(), current);
				} else {
					while (transaction != current && !m_stack.empty()) {
						m_validator.validate(m_stack.peek(), current);

						current = m_stack.pop();
					}
				}

				if (m_stack.isEmpty()) {
					MessageTree tree = m_tree.copy();

					m_tree.setMessageId(null);
					m_tree.setMessage(null);

					if (m_totalDurationInMicros > 0) {
						adjustForTruncatedTransaction((Transaction) tree.getMessage());
					}

					manager.flush(tree, true);
					return true;
				}
			}

			return false;
		}

		public boolean isTraceMode() {
			return m_traceMode;
		}

		public void setTraceMode(boolean traceMode) {
			m_traceMode = traceMode;
		}

		public void linkAsRunAway(DefaultForkedTransaction transaction) {
			m_validator.linkAsRunAway(transaction);
		}

		public Transaction peekTransaction(DefaultMessageManager defaultMessageManager) {
			if (m_stack.isEmpty()) {
				return null;
			} else {
				return m_stack.peek();
			}
		}

		public boolean shouldLog(Throwable e) {
			if (m_knownExceptions == null) {
				m_knownExceptions = new HashSet<Throwable>();
			}

			if (m_knownExceptions.contains(e)) {
				return false;
			} else {
				m_knownExceptions.add(e);
				return true;
			}
		}

		public void start(Transaction transaction, boolean forked) {
			if (!m_stack.isEmpty()) {
				// Do NOT make strong reference from parent transaction to forked transaction.
				// Instead, we create a "soft" reference to forked transaction later, via linkAsRunAway()
				// By doing so, there is no need for synchronization between parent and child threads.
				// Both threads can complete() anytime despite the other thread.
				if (!(transaction instanceof ForkedTransaction)) {
					Transaction parent = m_stack.peek();
					addTransactionChild(transaction, parent);
				}
			} else {
				m_tree.setMessage(transaction);
			}

			if (!forked) {
				m_stack.push(transaction);
			}
		}

		private long trimToHour(long timestamp) {
			return timestamp - timestamp % (3600 * 1000L);
		}
	}

	class TransactionHelper {
		private void linkAsRunAway(DefaultForkedTransaction transaction) {
			DefaultEvent event = new DefaultEvent("RemoteCall", "RunAway");

			event.addData(transaction.getForkedMessageId(), transaction.getType() + ":" + transaction.getName());
			event.setTimestamp(transaction.getTimestamp());
			event.setStatus(Message.SUCCESS);
			event.setCompleted(true);
			transaction.setStandalone(true);

			add(event);
		}

		private void markAsNotCompleted(DefaultTransaction transaction) {
			DefaultEvent event = new DefaultEvent("cat", "BadInstrument");

			event.setStatus("TransactionNotCompleted");
			event.setCompleted(true);
			transaction.addChild(event);
			transaction.setCompleted(true);
		}

		private void markAsRunAway(Transaction parent, DefaultTaggedTransaction transaction) {
			if (!transaction.hasChildren()) {
				transaction.addData("RunAway");
			}

			transaction.setStatus(Message.SUCCESS);
			transaction.setStandalone(true);
			transaction.complete();
		}

		private void migrateMessage(Stack<Transaction> stack, Transaction source, Transaction target, int level) {
			Transaction current = level < stack.size() ? stack.get(level) : null;
			boolean shouldKeep = false;

			for (Message child : source.getChildren()) {
				if (child != current) {
					target.addChild(child);
				} else {
					DefaultTransaction cloned = new DefaultTransaction(current.getType(), current.getName(),
											DefaultMessageManager.this);

					cloned.setTimestamp(current.getTimestamp());
					cloned.setDurationInMicros(current.getDurationInMicros());
					cloned.addData(current.getData().toString());
					cloned.setStatus(Message.SUCCESS);

					target.addChild(cloned);
					migrateMessage(stack, current, cloned, level + 1);
					shouldKeep = true;
				}
			}

			source.getChildren().clear();

			if (shouldKeep) { // add it back
				source.addChild(current);
			}
		}

		public void truncateAndFlush(Context ctx, long timestamp) {
			MessageTree tree = ctx.m_tree;
			Stack<Transaction> stack = ctx.m_stack;
			Message message = tree.getMessage();

			if (message instanceof DefaultTransaction) {
				String id = tree.getMessageId();

				if (id == null) {
					id = nextMessageId();
					tree.setMessageId(id);
				}

				String rootId = tree.getRootMessageId();
				String childId = nextMessageId();
				DefaultTransaction source = (DefaultTransaction) message;
				DefaultTransaction target = new DefaultTransaction(source.getType(), source.getName(), DefaultMessageManager.this);

				target.setTimestamp(source.getTimestamp());
				target.setDurationInMicros(source.getDurationInMicros());
				target.addData(source.getData().toString());
				target.setStatus(Message.SUCCESS);

				migrateMessage(stack, source, target, 1);

				for (int i = stack.size() - 1; i >= 0; i--) {
					DefaultTransaction t = (DefaultTransaction) stack.get(i);

					t.setTimestamp(timestamp);
					t.setDurationStart(System.nanoTime());
				}

				DefaultEvent next = new DefaultEvent("RemoteCall", "Next");

				next.addData(childId);
				next.setStatus(Message.SUCCESS);
				target.addChild(next);

				// tree is the parent, and m_tree is the child.
				MessageTree t = tree.copy();

				t.setMessage(target);

				ctx.m_tree.setMessageId(childId);
				ctx.m_tree.setParentMessageId(id);
				ctx.m_tree.setRootMessageId(rootId != null ? rootId : id);

				ctx.m_length = stack.size();
				ctx.m_totalDurationInMicros = ctx.m_totalDurationInMicros + target.getDurationInMicros();

				flush(t, false);
			}
		}

		public void validate(Transaction parent, Transaction transaction) {
			if (transaction.isStandalone()) {
				List<Message> children = transaction.getChildren();
				int len = children.size();

				for (int i = 0; i < len; i++) {
					Message message = children.get(i);

					if (message instanceof Transaction) {
						validate(transaction, (Transaction) message);
					}
				}

				if (!transaction.isCompleted() && transaction instanceof DefaultTransaction) {
					// missing transaction end, log a BadInstrument event so that
					// developer can fix the code
					markAsNotCompleted((DefaultTransaction) transaction);
				}
			} else if (!transaction.isCompleted()) {
				if (transaction instanceof DefaultForkedTransaction) {
					// link it as run away message since the forked transaction is not completed yet
					linkAsRunAway((DefaultForkedTransaction) transaction);
				} else if (transaction instanceof DefaultTaggedTransaction) {
					// link it as run away message since the forked transaction is not completed yet
					markAsRunAway(parent, (DefaultTaggedTransaction) transaction);
				}
			}
		}
	}
}
