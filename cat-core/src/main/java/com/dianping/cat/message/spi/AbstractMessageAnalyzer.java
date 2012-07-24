package com.dianping.cat.message.spi;

import com.dianping.cat.configuration.ServerConfigManager;
import com.site.lookup.ContainerHolder;

public abstract class AbstractMessageAnalyzer<R> extends ContainerHolder implements MessageAnalyzer {
	private volatile boolean m_active = true;

	protected static final long MINUTE = 60 * 1000;

	private long m_errors = 9999;

	@Override
	public void analyze(MessageQueue queue) {
		while (!isTimeout() && isActive()) {
			MessageTree tree = queue.poll();

			if (tree != null) {
				try {
					process(tree);
				} catch (Throwable e) {
					m_errors++;
					if (m_errors % 10000 == 0) {
						e.printStackTrace();
					}
				}
			}
		}

		while (true) {
			MessageTree tree = queue.poll();

			if (tree != null) {
				try {
					process(tree);
				} catch (Throwable e) {
					m_errors++;
					if (m_errors % 10000 == 0) {
						e.printStackTrace();
					}
				}
			} else {
				break;
			}
		}
	}

	protected boolean isActive() {
		synchronized (this) {
			return m_active;
		}
	}

	public void shutdown() {
		synchronized (this) {
			m_active = false;
		}
	}

	protected boolean isLocalMode() {
		ServerConfigManager manager = lookup(ServerConfigManager.class);

		return manager.isLocalMode();
	}

	public void doCheckpoint(boolean atEnd) {
		// override it
	}

	public abstract R getReport(String domain);

	protected abstract boolean isTimeout();

	protected abstract void process(MessageTree tree);
}
