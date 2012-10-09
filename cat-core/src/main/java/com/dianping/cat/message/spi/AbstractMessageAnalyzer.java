package com.dianping.cat.message.spi;

import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.Transaction;
import com.site.lookup.ContainerHolder;

public abstract class AbstractMessageAnalyzer<R> extends ContainerHolder implements MessageAnalyzer {

	protected static final long MINUTE = 60 * 1000;

	protected long m_extraTime;

	protected long m_startTime;

	protected long m_duration;

	protected Logger m_logger;

	private long m_errors = 0;

	private volatile boolean m_active = true;

	@Override
	public void analyze(MessageQueue queue) {
		while (!isTimeout() && isActive()) {
			MessageTree tree = queue.poll();

			if (tree != null) {
				try {
					process(tree);
				} catch (Throwable e) {
					m_errors++;

					if (m_errors == 1 || m_errors % 10000 == 0) {
						Cat.logError(e);
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

					if (m_errors == 1 || m_errors % 10000 == 0) {
						Cat.logError(e);
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

	protected boolean shouldDiscard(Transaction t) {
		// pigeon default heartbeat is no use
		String type = t.getType();
		String name = t.getName();
		
		if ((("Service").equals(type) || ("PigeonService").equals(type))
		      && (("piegonService:heartTaskService:heartBeat").equals(name)
		            || ("piegonService:heartTaskService:heartBeat()").equals(name) || ("pigeon:HeartBeatService:null")
		               .equals(name))) {
			return true;
		}
		return false;
	}

	public abstract R getReport(String domain);

	protected abstract boolean isTimeout();

	protected abstract void process(MessageTree tree);
}
