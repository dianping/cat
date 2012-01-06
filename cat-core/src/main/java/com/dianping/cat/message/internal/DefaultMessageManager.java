package com.dianping.cat.message.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Stack;

import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.io.MessageSender;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.lookup.ContainerHolder;

public class DefaultMessageManager extends ContainerHolder implements MessageManager {
	private TransportManager m_manager;

	// we don't use static modifier since MessageManager is a singleton in
	// production actually
	private InheritableThreadLocal<Context> m_context = new InheritableThreadLocal<Context>() {
		@Override
		protected Context initialValue() {
			return null;
		}
	};

	private Config m_config;

	private String m_domain;

	private String m_hostName;

	private String m_ipAddress;

	@Override
	public void add(Message message) {
		getContext().add(this, message);
	}

	@Override
	public void end(Transaction transaction) {
		getContext().end(this, transaction);
	}

	void flush(MessageTree tree) {
		// if (m_manager == null) {
		// throw new
		// RuntimeException("Cat has not been initialized successfully, please call Cat.initialize() first!");
		// }

		if (m_manager != null) {
			MessageSender sender = m_manager.getSender();

			if (sender != null) {
				sender.send(tree);
			}
		}
	}

	@Override
	public Config getConfig() {
		return m_config;
	}

	Context getContext() {
		Context ctx = m_context.get();

		if (ctx == null) {
			throw new RuntimeException(
			      "Cat has not been initialized successfully, please call Cal.setup(...) first for each thread.");
		} else {
			return ctx;
		}
	}

	@Override
	public void initialize(Config config) {
		m_config = config;

		if (m_config != null && m_config.getApp() != null) {
			m_domain = m_config.getApp().getDomain();
		}

		try {
			InetAddress localHost = InetAddress.getLocalHost();

			m_hostName = localHost.getHostName();
			m_ipAddress = localHost.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		m_manager = lookup(TransportManager.class);
	}

	@Override
	public void reset() {
		// destroy current thread local data
		m_context.remove();
	}

	@Override
	public void setup(String sessionToken, String requestToken) {
		Context ctx = new Context(m_domain, m_hostName, m_ipAddress, sessionToken, requestToken);

		m_context.set(ctx);
	}

	@Override
	public void start(Transaction transaction) {
		getContext().start(transaction);
	}

	static class Context {
		private MessageTree m_tree;

		private Stack<Transaction> m_stack;

		public Context(String domain, String hostName, String ipAddress, String sessionToken, String requestToken) {
			m_tree = new DefaultMessageTree();
			m_stack = new Stack<Transaction>();

			m_tree.setDomain(domain);
			m_tree.setSessionToken(sessionToken);
			m_tree.setRequestToken(requestToken);
			m_tree.setThreadId(Long.toHexString(Thread.currentThread().getId()));
			m_tree.setHostName(hostName);
			m_tree.setIpAddress(ipAddress);
			m_tree.setMessageId("?"); // TODO
		}

		public void add(DefaultMessageManager manager, Message message) {
			if (m_stack.isEmpty()) {
				m_tree.setMessage(message);
				manager.flush(m_tree);
			} else {
				Transaction entry = m_stack.peek();

				entry.addChild(message);
			}
		}

		public void end(DefaultMessageManager manager, Transaction transaction) {
			if (!m_stack.isEmpty()) {
				Transaction current = m_stack.peek();

				if (transaction.equals(current)) {
					validateTransaction(current);
				} else {
					throw new RuntimeException("Internal error: Transaction logging mismatched!");
				}

				m_stack.pop();

				if (m_stack.isEmpty()) {
					manager.flush(m_tree);
				}
			}
		}

		public void start(Transaction transaction) {
			if (!m_stack.isEmpty()) {
				Transaction entry = m_stack.peek();

				entry.addChild(transaction);
			} else {
				m_tree.setMessage(transaction);
			}

			m_stack.push(transaction);
		}

		void validateTransaction(Transaction transaction) {
			for (Message message : transaction.getChildren()) {
				if (message.getStatus() == null) {
					message.setStatus("unset");
				}

				if (!message.isCompleted() && message instanceof DefaultTransaction) {
					DefaultTransaction t = (DefaultTransaction) message;

					validateTransaction(t);

					// missing transaction end, log a BadInstrument event so that
					// developer can fix the code
					DefaultEvent notCompleteEvent = new DefaultEvent("CAT", "BadInstrument");

					notCompleteEvent.setStatus("TransactionNotCompleted");
					notCompleteEvent.setCompleted(true);
					transaction.addChild(notCompleteEvent);
					t.setCompleted(true);
				}
			}
		}
	}
}
