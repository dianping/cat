package com.dianping.cat.message.internal;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.io.MessageSender;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class DefaultMessageManager extends ContainerHolder implements MessageManager, Initializable, LogEnabled {
	@Inject
	private ClientConfigManager m_configManager;

	@Inject
	private TransportManager m_transportManager;

	@Inject
	private MessageStatistics m_statistics;

	// we don't use static modifier since MessageManager is a singleton actually
	private ThreadLocal<Context> m_context = new ThreadLocal<Context>();

	private InheritableThreadLocal<String> m_inheritableContext = new InheritableThreadLocal<String>();

	private MessageIdFactory m_factory;

	private long m_throttleTimes;

	private Domain m_domain;

	private String m_hostName;

	private Logger m_logger;

	private boolean m_firstMessage = true;

	@Override
	public void add(Message message) {
		Context ctx = getContext();

		if (ctx != null) {
			ctx.add(this, message);
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

	public void flush(MessageTree tree) {
		MessageSender sender = m_transportManager.getSender();

		if (sender != null && isCatEnabled()) {
			sender.send(tree);

			if (m_statistics != null) {
				m_statistics.onSending(tree);
			}
		} else {
			m_throttleTimes++;

			if (m_throttleTimes % 10000 == 0 || m_throttleTimes == 1) {
				m_logger.info("Cat Message is throttled! Times:" + m_throttleTimes);
			}
		}
	}

	public ClientConfigManager getConfigManager() {
		return m_configManager;
	}

	private Context getContext() {
		if (Cat.isInitialized()) {
			Context ctx = m_context.get();

			if (ctx != null) {
				return ctx;
			}
		}

		return null;
	}

	public String getMetricType() {
		return m_inheritableContext.get();
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

		if (ctx != null) {
			return ctx.m_tree;
		} else {
			return null;
		}
	}

	Set<Throwable> getKnownExceptions() {
		Context ctx = m_context.get();

		if (ctx != null) {
			return ctx.getKnownExceptions();
		} else {
			return null;
		}
	}

	@Override
	public boolean hasContext() {
		return m_context.get() != null;
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
			m_factory = lookup(MessageIdFactory.class);
			m_factory.initialize(m_domain.getId());
		} catch (IOException e) {
			throw new InitializationException("Error while initializing MessageIdFactory!", e);
		}
	}

	@Override
	public boolean isCatEnabled() {
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

	private String nextMessageId() {
		return m_factory.getNextId();
	}

	@Override
	public void reset() {
		// destroy current thread local data
		Context ctx = m_context.get();

		if (ctx != null) {
			ctx.m_stack.clear();
		}

		m_context.remove();
	}

	public void setMetricType(String metricType) {
		m_inheritableContext.set(metricType);
	}

	public void setTraceMode(boolean traceMode) {
		Context content = getContext();

		if (content != null) {
			content.setTraceMode(traceMode);
		}
	}

	@Override
	public void setup() {
		Context ctx;

		if (m_domain != null) {
			ctx = new Context(m_domain.getId(), m_hostName, m_domain.getIp(), m_configManager);
		} else {
			ctx = new Context("Unknown", m_hostName, "", m_configManager);
		}

		m_context.set(ctx);
	}

	@Override
	public void start(Transaction transaction) {
		Context ctx = getContext();

		if (ctx != null) {
			ctx.start(this, transaction);
		} else if (m_firstMessage) {
			m_firstMessage = false;
			m_logger.warn("CAT client is not enabled because it's not initialized yet");
		}
	}

	static class Context {
		private MessageTree m_tree;

		private Stack<Transaction> m_stack;

		private ClientConfigManager m_configManager;

		private int m_length;

		private long m_totalDurationInMicros; // for truncate message

		private boolean m_traceMode = false;

		private Set<Throwable> m_knownExceptions = new HashSet<Throwable>();

		public Context(String domain, String hostName, String ipAddress, ClientConfigManager configManager) {
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
			m_configManager = configManager;
			m_length = 1;
		}

		public Set<Throwable> getKnownExceptions() {
			return m_knownExceptions;
		}

		public void add(DefaultMessageManager manager, Message message) {
			if (m_stack.isEmpty()) {
				MessageTree tree = m_tree.copy();

				if (tree.getMessageId() == null) {
					tree.setMessageId(manager.nextMessageId());
				}

				tree.setMessage(message);
				manager.flush(tree);
			} else {
				Transaction parent = m_stack.peek();

				addTransactionChild(manager, message, parent);
			}
		}

		private void addTransactionChild(DefaultMessageManager manager, Message message, Transaction transaction) {
			long treePeriod = trimToHour(m_tree.getMessage().getTimestamp());
			long messagePeriod = trimToHour(message.getTimestamp() - 10 * 1000L); // 10 seconds extra time allowed

			if (treePeriod < messagePeriod || m_length >= m_configManager.getMaxMessageLength()) {
				truncateAndFlushMessage(manager, message.getTimestamp());
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
					validateTransaction(current);
				} else {
					while (transaction != current && !m_stack.empty()) {
						validateTransaction(current);

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

					manager.flush(tree);
					return true;
				}
			}

			return false;
		}

		public boolean isTraceMode() {
			return m_traceMode;
		}

		private void migrateMessage(DefaultMessageManager manager, Transaction source, Transaction target, int level) {
			Transaction current = level < m_stack.size() ? m_stack.get(level) : null;
			boolean shouldKeep = false;

			for (Message child : source.getChildren()) {
				if (child != current) {
					target.addChild(child);
				} else {
					DefaultTransaction cloned = new DefaultTransaction(current.getType(), current.getName(), manager);

					cloned.setTimestamp(current.getTimestamp());
					cloned.setDurationInMicros(current.getDurationInMicros());
					cloned.addData(current.getData().toString());
					cloned.setStatus(Message.SUCCESS);

					target.addChild(cloned);
					migrateMessage(manager, current, cloned, level + 1);
					shouldKeep = true;
				}
			}

			source.getChildren().clear();

			if (shouldKeep) { // add it back
				source.addChild(current);
			}
		}

		public Transaction peekTransaction(DefaultMessageManager defaultMessageManager) {
			if (m_stack.isEmpty()) {
				return null;
			} else {
				return m_stack.peek();
			}
		}

		public void setTraceMode(boolean traceMode) {
			m_traceMode = traceMode;
		}

		public void start(DefaultMessageManager manager, Transaction transaction) {
			if (!m_stack.isEmpty()) {
				Transaction parent = m_stack.peek();

				addTransactionChild(manager, transaction, parent);
			} else {
				if (m_tree.getMessageId() == null) {
					m_tree.setMessageId(manager.nextMessageId());
				}

				m_tree.setMessage(transaction);
			}
			m_stack.push(transaction);
		}

		private long trimToHour(long timestamp) {
			return timestamp - timestamp % (3600 * 1000L);
		}

		private void truncateAndFlushMessage(DefaultMessageManager manager, long timestamp) {
			Message message = m_tree.getMessage();

			if (message instanceof DefaultTransaction) {
				String id = m_tree.getMessageId();
				String rootId = m_tree.getRootMessageId();
				String childId = manager.nextMessageId();
				DefaultTransaction source = (DefaultTransaction) message;
				DefaultTransaction target = new DefaultTransaction(source.getType(), source.getName(), manager);

				target.setTimestamp(source.getTimestamp());
				target.setDurationInMicros(source.getDurationInMicros());
				target.addData(source.getData().toString());
				target.setStatus(Message.SUCCESS);

				migrateMessage(manager, source, target, 1);

				for (int i = m_stack.size() - 1; i >= 0; i--) {
					DefaultTransaction t = (DefaultTransaction) m_stack.get(i);

					t.setTimestamp(timestamp);
				}

				DefaultEvent next = new DefaultEvent("RemoteCall", "Next");

				next.addData(childId);
				next.setStatus(Message.SUCCESS);
				target.addChild(next);

				// tree is the parent, and m_tree is the child.
				MessageTree tree = m_tree.copy();

				tree.setMessage(target);

				manager.flush(tree);
				m_tree.setMessageId(childId);
				m_tree.setParentMessageId(id);
				m_tree.setRootMessageId(rootId != null ? rootId : id);
				m_length = m_stack.size();
				m_totalDurationInMicros = m_totalDurationInMicros + target.getDurationInMicros();
			}
		}

		void validateTransaction(Transaction transaction) {
			if (!transaction.isStandalone()) {
				return;
			}

			List<Message> children = transaction.getChildren();
			int len = children.size();

			for (int i = 0; i < len; i++) {
				Message message = children.get(i);

				if (message instanceof Transaction) {
					validateTransaction((Transaction) message);
				}
			}

			if (!transaction.isCompleted() && transaction.isStandalone() && transaction instanceof DefaultTransaction) {
				// missing transaction end, log a BadInstrument event so that
				// developer can fix the code
				DefaultEvent notCompleteEvent = new DefaultEvent("CAT", "BadInstrument");

				notCompleteEvent.setStatus("TransactionNotCompleted");
				notCompleteEvent.setCompleted(true);
				transaction.addChild(notCompleteEvent);
				((DefaultTransaction) transaction).setCompleted(true);
			}
		}
	}
}
