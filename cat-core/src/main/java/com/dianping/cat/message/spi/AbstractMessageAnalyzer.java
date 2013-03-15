package com.dianping.cat.message.spi;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.ContainerHolder;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.Transaction;

public abstract class AbstractMessageAnalyzer<R> extends ContainerHolder implements MessageAnalyzer {

	protected static final long MINUTE = 60 * 1000;

	protected long m_extraTime;

	protected long m_startTime;

	protected long m_duration;

	protected Logger m_logger;

	private long m_errors = 0;

	private volatile boolean m_active = true;

	protected static final String ALL = "All";

	protected static Set<String> UNUSED_TYPES = new HashSet<String>();

	protected static Set<String> UNUSED_NAMES = new HashSet<String>();

	static {
		UNUSED_TYPES.add("Service");
		UNUSED_TYPES.add("PigeonService");
		UNUSED_NAMES.add("piegonService:heartTaskService:heartBeat");
		UNUSED_NAMES.add("piegonService:heartTaskService:heartBeat()");
		UNUSED_NAMES.add("pigeon:HeartBeatService:null");
	}

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

		if (UNUSED_TYPES.contains(type) && UNUSED_NAMES.contains(name)) {
			return true;
		}
		return false;
	}

	public abstract R getReport(String domain);

	protected abstract boolean isTimeout();

	protected abstract void process(MessageTree tree);
}
