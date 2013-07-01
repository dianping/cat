package com.dianping.cat.message.internal;

import java.io.IOException;
import java.util.List;
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

	private static final int MAX_ITEM = 500;

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

	void flush(MessageTree tree) {
		MessageSender sender = m_transportManager.getSender();

		if (sender != null && !shouldThrottle(tree)) {
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

	Context getContext() {
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

	@Override
	public boolean hasContext() {
		return m_context.get() != null;
	}

	@Override
	public void initialize() throws InitializationException {
		m_domain = m_configManager.getFirstDomain();
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

	String nextMessageId() {
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

	@Override
	public void setup() {
		Context ctx;

		if (m_domain != null) {
			ctx = new Context(m_domain.getId(), m_hostName, m_domain.getIp());
		} else {
			ctx = new Context("Unknown", m_hostName, "");
		}

		m_context.set(ctx);
	}

	private boolean shouldThrottle(MessageTree tree) {
		if (!isCatEnabled()) {
			return true;
		}

		return false;
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
				Transaction entry = m_stack.peek();

				addTransactionChild(message, entry);
			}
		}

		private void addTransactionChild(Message message, Transaction transaction) {
			List<Message> children = transaction.getChildren();

			if (children != null && children.size() < MAX_ITEM) {
				transaction.addChild(message);
			} else {
				try {
					if (children.size() == MAX_ITEM) {
						DefaultEvent event = new DefaultEvent("CAT", "TooManyChildren");

						event.setStatus(String.valueOf(MAX_ITEM + 1));
						transaction.addChild(event);
					} else {
						Message event = children.get(MAX_ITEM);
						String count = event.getStatus();

						event.setStatus(String.valueOf((Integer.parseInt(count) + 1)));
					}
				} catch (Exception e) {
					// ignore
				}
			}
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
					manager.flush(tree);
					return true;
				}
			}

			return false;
		}

		public Transaction peekTransaction(DefaultMessageManager defaultMessageManager) {
			if (m_stack.isEmpty()) {
				return null;
			} else {
				return m_stack.peek();
			}
		}

		public void start(DefaultMessageManager manager, Transaction transaction) {
			if (!m_stack.isEmpty()) {
				Transaction entry = m_stack.peek();

				addTransactionChild(transaction, entry);
			} else {
				if (m_tree.getMessageId() == null) {
					m_tree.setMessageId(manager.nextMessageId());
				}

				m_tree.setMessage(transaction);
			}

			m_stack.push(transaction);
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
